package com.ningmeng.ucenter.controller;

import com.ningmeng.api.ucenterapi.UcenterControllerApi;
import com.ningmeng.framework.domain.ucenter.ext.NmUserExt;
import com.ningmeng.ucenter.service.UcenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by 86181 on 2020/3/11.
 */
@RestController
@RequestMapping("/ucenter")
public class UcenterController implements UcenterControllerApi {

    @Autowired
    private UcenterService ucenterService;

    @Override
    @GetMapping("/getuserext")
    public NmUserExt getUserext(@RequestParam("username") String username) {
        NmUserExt nmUser = ucenterService.getUserExt(username);
        return nmUser;
    }
}
