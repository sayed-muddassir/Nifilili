package com.nifilili.auth.service;

import com.nifilili.auth.dto.request.LoginDto;
import com.nifilili.auth.dto.request.RegisterDto;
import com.nifilili.auth.dto.response.JwtAuthResponse;
import com.nifilili.auth.dto.response.UserProfileResponse;

public interface AuthService {

    JwtAuthResponse login(LoginDto loginDto);

    JwtAuthResponse register(RegisterDto registerDto);

    UserProfileResponse getCurrentUser();
}
