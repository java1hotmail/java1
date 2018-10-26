/*
 * Copyright 2018, AT&T Intellectual Property. All rights reserved.
 */
package com.att.cso.reports.web.controllers;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.jasypt.digest.StandardStringDigester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
//import org.apache.maven.model.Model;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.att.cso.reports.data.models.Device;
import com.att.cso.reports.data.models.User;
import com.att.cso.reports.enums.EDeviceType;
import com.att.cso.reports.services.ActionService;
import com.att.cso.reports.services.DeviceService;
import com.att.cso.reports.services.UserService;
import com.att.cso.reports.web.DeviceDTO;
import com.att.cso.reports.web.links.UserLinks;

/**
 * Spring MVC controller to handle requests for CSO Reports devices.
 *
 * @author Franklin Mosley (fm1376)
 *
 */
@Controller
@RequestMapping({ "/devices" })
public class DeviceController extends BaseController {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(DeviceController.class);

    private static final String SECRET = "device.registration.secret";

    private static final String TEMPLATE_DEVICE_INFO = "template.device.info";

    private static final String TEMPLATE_DEVICES = "template.device.all";

    private static final String TEMPLATE_DEVICE_FRAG_FILTER = "template.device.frag.filter";

    private static final int BUTTONS_TO_SHOW = 3;

    private static final int INITIAL_PAGE = 0;

    private static final int INITIAL_PAGE_SIZE = 5;

    private final String IOS = "4";

    private static final int[] PAGE_SIZES = { INITIAL_PAGE_SIZE, 10 };

    private static final String DEVICES = "devices";

    private static final String REQ = "Request";

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private UserService userService;

    @Autowired
    UserLinks userLinks;

    @Autowired
    MessageSource messageSource;

    @Autowired
    ActionService actionService;

    @Autowired
    ThreadPoolTaskExecutor taskExecutor;

    @Resource
    private Environment environment;

    @RequestMapping(value = "/ios/register", method = RequestMethod.POST)
    public ResponseEntity<String> addiOSDevice(@Valid DeviceDTO deviceDTO,
        BindingResult bindingResult) {

        loggerRequest(deviceDTO, "Apple ios ");
        if (bindingResult.hasErrors()) {
            loggerError(bindingResult);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
        // Check the secret
        final String secret = this.environment.getRequiredProperty(SECRET);
        final StandardStringDigester digester = new StandardStringDigester();
        digester.setAlgorithm("SHA-256"); // Set the algorithm

        if (!digester.matches(deviceDTO.getSecret(), secret)) {
            return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
        }

        // Add an iOS Device Token
        final Device device = deviceService.addDevice(deviceDTO,
            EDeviceType.IOS);

        if (null == device) {
            // TODO Check the best return code here
            LOGGER.error("Error saving device to DB.");
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @RequestMapping(value = "/android/register", method = RequestMethod.POST)
    public ResponseEntity<String> addAndroidDevice(@Valid DeviceDTO deviceDTO,
        BindingResult bindingResult) {

        loggerRequest(deviceDTO, "Android ");
        if (bindingResult.hasErrors()) {
            loggerError(bindingResult);

            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }

        // Check the secret
        String secret = this.environment.getRequiredProperty(SECRET);
        StandardStringDigester digester = new StandardStringDigester();
        digester.setAlgorithm("SHA-256"); // Set the algorithm

        if (!digester.matches(deviceDTO.getSecret(), secret)) {
            return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
        }

        // Addfinal Device device = deviceService.addDevice(deviceDTO,
        // EDeviceType.ANDROID);
        Device device = deviceService.addDevice(deviceDTO, EDeviceType.ANDROID);
        if (null == device) {
            // TODO Check the best return code here
            LOGGER.error("Error saving device to DB.");
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @RequestMapping(value = "/wp/register", method = RequestMethod.POST)
    public ResponseEntity<String> addWindowsToken(@Valid DeviceDTO deviceDTO,
        BindingResult bindingResult) {

        loggerRequest(deviceDTO, "Window ");
        if (bindingResult.hasErrors()) {
            loggerError(bindingResult);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }

        // Check the secret
        final String secret = this.environment.getRequiredProperty(SECRET);
        final StandardStringDigester digester = new StandardStringDigester();
        digester.setAlgorithm("SHA-256"); // Set the algorithm

        if (!digester.matches(deviceDTO.getSecret(), secret)) {
            return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
        }

        // Add a Windows Device Token
        final Device device = deviceService.addDevice(deviceDTO,
            EDeviceType.WINDOWS);

        if (null == device) {
            // TODO Check the best return code here
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @RequestMapping(value = "/wp", method = RequestMethod.GET,
        params = { "tokens" })
    public void downloadWpTokens(Principal principal,
        final HttpServletRequest req,
        HttpServletResponse response) throws IOException {

        // Get the authorized user
        final User authorizedUser = getAuthorizedUser(principal);

        response.setContentType("text/plain");
        response.setHeader("Content-Disposition",
            "attachment; filename=\"wptokens.txt\"");
        final ServletOutputStream out = response.getOutputStream();

        if (userService.isWindows(authorizedUser)) {
            List<Device> devices = deviceService
                .getDevicesByDeviceType(EDeviceType.WINDOWS.getName());
            if (null != devices && devices.size() > 0) {
                for (Device device : devices) {
                    out.println(device.getDeviceToken());
                }

                out.flush();
                out.close();
                return;
            }
        }
        out.println("");
        out.flush();
        out.close();
        return;
    }

    @RequestMapping(value = "/deviceinfo", method = RequestMethod.GET)
    public String getDevices(Principal principal,
        @PageableDefault(page = 0, size = 10, sort = "user",
            direction = Direction.ASC) Pageable pageable,
        Model model) {

        final User authorizedUser = getAuthorizedUser(principal);
        if (null == authorizedUser) {
            return redirect("/dashboard");
        }
        // Page<Device> pageDevices = deviceService.getDevicesByPage(pageable);
        final List<Device> devices = deviceService.getDevicesByUser();
        model.addAttribute(DEVICES, devices);
        model.addAttribute("isAdmin",
            userService.isAdministrator(authorizedUser));
        model.addAttribute("isMetrics", userService.isMetrics(authorizedUser));
        model.addAttribute("firstname", authorizedUser.getFirstname());
        return environment.getRequiredProperty(TEMPLATE_DEVICE_INFO);
    }

    @RequestMapping(value = "/notify", method = RequestMethod.POST)
    @ResponseBody // for ajax, must have this
    public String sendNotify(Principal principal,
        @RequestParam("message") String message,
        @RequestParam("list") String devices) {

        int nbrOfDevices = 0;
        String rtnmsg = "";
        LOGGER.info("devices: {}", devices);
        final User authorizedUser = getAuthorizedUser(principal);
        if (null == authorizedUser) {
            return redirect("/dashboard");
        }
        nbrOfDevices = actionService.deviceAPNotification(devices, message);
        return nbrOfDevices + " notification were sent!";
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String removeToken(Principal principal,
        @RequestParam("list") String devices) {

        if (devices == null || devices.length() < 1)
            return "No token is removed!";
        final ArrayList<String> ios = new ArrayList<String>();
        final ArrayList<String> andrs = new ArrayList<String>();
        final ArrayList<String> wins = new ArrayList<String>();
        for (String token : devices.split(",")) {
            String[] token2 = token.split("##");
            if (token2[0].equalsIgnoreCase(IOS)) {
                ios.add(token2[1]);
            }
        }
        if (ios.isEmpty())
            return "No IOS device is found!";
        int nbrOfDevices = ios.size();
        LOGGER.info("Devices: {}", ios);
        final User authorizedUser = getAuthorizedUser(principal);
        if (null == authorizedUser) {
            return redirect("/dashboard");
        }
        deviceService.removeDeviceTokens(ios);
        return nbrOfDevices + " tocken(s) were removed!";

    }

    void loggerRequest(DeviceDTO deviceDTO, String os) {

        LOGGER.info("Received a request to register a: {}", os);
        LOGGER.info(REQ + " User: {}", deviceDTO.getUser());
        LOGGER.debug(REQ + " Token: {}", deviceDTO.getToken());
        LOGGER.info(REQ + " Name: {}", deviceDTO.getName());
        LOGGER.debug(REQ + " UUID: {}", deviceDTO.getUuid());
        LOGGER.info(REQ + " App Version: {}", deviceDTO.getAppVersion());
        LOGGER.debug(REQ + " Manufacturer: {}", deviceDTO.getManufacturer());
        LOGGER.debug(REQ + " Model: {}", deviceDTO.getModel());
        LOGGER.debug(REQ + " OS: {}", deviceDTO.getOs());
        LOGGER.debug(REQ + " OS Version: {}", deviceDTO.getOsVersion());
    }

    void loggerError(BindingResult bindingResult) {

        LOGGER.debug("Device Token Form had errors: {}",
            bindingResult.getFieldError().getDefaultMessage());
        final List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            LOGGER.debug("Field Error: {}", fieldError);
        }
    }
}
