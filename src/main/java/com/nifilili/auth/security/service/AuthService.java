package com.nifilili.auth.security.service;

import com.nifilili.auth.security.dto.request.LoginDto;

public interface AuthService {
    String login(LoginDto loginDto);
}
