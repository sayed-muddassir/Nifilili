package com.nifilili.auth.service;

import com.nifilili.auth.dto.request.LoginDto;

public interface AuthService {
    String login(LoginDto loginDto);
}
