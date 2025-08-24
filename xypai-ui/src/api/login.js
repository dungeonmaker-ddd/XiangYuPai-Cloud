import request from '@/utils/request'

// WEB端登录方法
export function login(username, password, code, uuid) {
  return request({
    url: '/auth/login',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data: {
      username,
      password,
      code,
      uuid,
      clientType: 'web'  // 固定为WEB端
    }
  })
}


// WEB端注册方法
export function register(data) {
  return request({
    url: '/auth/register',
    headers: {
      isToken: false
    },
    method: 'post',
    data: {
      ...data,
      clientType: 'web'  // 固定为WEB端
    }
  })
}

// WEB端Token刷新方法
export function refreshToken() {
  return request({
    url: '/auth/refresh',
    method: 'post'
  })
}

// 获取用户详细信息
export function getInfo() {
  return request({
    url: '/system/user/getInfo',
    method: 'get'
  })
}

// 退出方法
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'delete'
  })
}

// 获取当前用户信息
export function getCurrentUserInfo() {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}

// 验证token有效性
export function validateToken() {
  return request({
    url: '/auth/validate',
    method: 'get'
  })
}

// 修改密码
export function changePassword(oldPassword, newPassword) {
  return request({
    url: '/auth/password',
    method: 'put',
    data: {
      oldPassword,
      newPassword
    }
  })
}

// 获取验证码
export function getCodeImg() {
  return request({
    url: '/code',
    headers: {
      isToken: false
    },
    method: 'get',
    timeout: 20000
  })
}

// 导出认证类型常量
export const AuthType = {
  WEB: 'web'
}

// 导出登录状态常量
export const LoginStatus = {
  SUCCESS: 200,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  LOCKED: 423,
  SERVER_ERROR: 500
}
