package com.global.settings;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

// PasswordFormValidator 는 Bean 으로 등록하지 않음
// new 로 생성하든지 SettingsController 에서 @InitBinder("passwordForm")로 처리함
// 여기서는 SettingsController 에서 @InitBinder("passwordForm")로 처리함
public class PasswordFormValidator implements Validator {

  // 어떤 type 의 Form 객체를 검증하는가?
  @Override
  public boolean supports(Class<?> clazz) {
    // PasswordForm type 에 할당 가능한 type 이어야만 검증하겠다는 의미
    return PasswordForm.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    // PasswordFrom 으로 형변환해서 이 둘이 같은지 비교함
    PasswordForm passwordForm = (PasswordForm)target;

    // password 가 다른 경우
    if(!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())){
      errors.rejectValue("newPassword", "wrong.value", "입력하신 비밀번호가 일치하지 않습니다");
    }


  }
}
