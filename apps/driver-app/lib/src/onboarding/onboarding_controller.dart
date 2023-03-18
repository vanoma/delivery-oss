// ignore_for_file: prefer_final_fields

import 'dart:convert';
import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:jwt_decoder/jwt_decoder.dart';
import 'package:onesignal_flutter/onesignal_flutter.dart';
import 'package:vanoma_driver/src/account/account_controller.dart';
import 'package:vanoma_driver/src/account/models/driver.dart';
import 'package:vanoma_driver/src/models/http_exception.dart';
import 'package:vanoma_driver/src/onboarding/onboarding_service.dart';

class OnboardingController with ChangeNotifier {
  OnboardingController(this._onboardingService, this._accountController);
  final OnboardingService _onboardingService;
  final AccountController _accountController;

  ValidationItem _firstName = ValidationItem(error: 'First name is required');
  ValidationItem _lastName = ValidationItem(error: 'Last name is required');
  ValidationItem _phoneNumber =
      ValidationItem(error: 'Phone number is required');
  ValidationItem _secondPhoneNumber =
      ValidationItem(error: 'Another phone number is required');
  ValidationItem _password = ValidationItem(error: 'Password is required');
  ValidationItem _confirmPassword =
      ValidationItem(error: 'Confirm password is required');
  ValidationItem _verificationCode =
      ValidationItem(error: 'Verification code is required');

  String? _verificationId;
  String? _accessToken;
  String? _userId;
  String? _driverId;

  ValidationItem get firstName => _firstName;
  ValidationItem get lastName => _lastName;
  ValidationItem get phoneNumber => _phoneNumber;
  ValidationItem get secondPhoneNumber => _secondPhoneNumber;
  ValidationItem get password => _password;
  ValidationItem get confirmPassword => _confirmPassword;
  ValidationItem get verificationCode => _verificationCode;

  String? get userId => _userId;
  String? get driverId => _driverId;

  void changeFirstName(String value) {
    String firstName = value.trim();
    if (firstName.isEmpty) {
      _firstName = ValidationItem(
        error: 'First name is required',
        isValidating: _firstName.isValidating,
      );
    } else {
      if (firstName.length >= 2) {
        _firstName = ValidationItem(
          value: firstName,
          isValidating: _firstName.isValidating,
        );
      } else {
        _firstName = ValidationItem(
          error: 'First name is too short',
          isValidating: _firstName.isValidating,
        );
      }
    }
    notifyListeners();
  }

  void changeLastName(String value) {
    String lastName = value.trim();
    if (lastName.isEmpty) {
      _lastName = ValidationItem(
        error: 'Last name is required',
        isValidating: _lastName.isValidating,
      );
    } else {
      if (lastName.length >= 2) {
        _lastName = ValidationItem(
          value: lastName,
          isValidating: _lastName.isValidating,
        );
      } else {
        _lastName = ValidationItem(
          error: 'Last name is too short',
          isValidating: _lastName.isValidating,
        );
      }
    }
    notifyListeners();
  }

  void changePhoneNumber(String value) {
    String number = value.trim();
    if (number.isEmpty) {
      _phoneNumber = ValidationItem(
        error: 'Phone number is required',
        isValidating: _phoneNumber.isValidating,
      );
    } else {
      try {
        int.parse(number);
        if (number.startsWith('07') && number.length == 10) {
          _phoneNumber = ValidationItem(
            value: number,
            isValidating: _phoneNumber.isValidating,
          );
        } else if (number.startsWith('2507') && number.length == 12) {
          _phoneNumber = ValidationItem(
            value: number,
            isValidating: _phoneNumber.isValidating,
          );
        } else {
          _phoneNumber = ValidationItem(
            error: 'Phone number is invalid',
            isValidating: _phoneNumber.isValidating,
          );
        }
      } catch (_) {
        _phoneNumber = ValidationItem(
          error: 'Phone number must be a number',
          isValidating: _phoneNumber.isValidating,
        );
      }
    }
    notifyListeners();
  }

  void changeSecondPhoneNumber(String value) {
    String number = value.trim();
    if (number.isEmpty) {
      _secondPhoneNumber = ValidationItem(
        error: 'Phone number is required',
        isValidating: _secondPhoneNumber.isValidating,
      );
    } else {
      try {
        int.parse(number);
        if (number.startsWith('07') && number.length == 10) {
          _secondPhoneNumber = ValidationItem(
            value: number,
            isValidating: _secondPhoneNumber.isValidating,
          );
        } else if (number.startsWith('2507') && number.length == 12) {
          _secondPhoneNumber = ValidationItem(
            value: number,
            isValidating: _secondPhoneNumber.isValidating,
          );
        } else {
          _secondPhoneNumber = ValidationItem(
            error: 'Phone number is invalid',
            isValidating: _secondPhoneNumber.isValidating,
          );
        }
      } catch (_) {
        _secondPhoneNumber = ValidationItem(
          error: 'Phone number must be a number',
          isValidating: _secondPhoneNumber.isValidating,
        );
      }
    }
    notifyListeners();
  }

  void changePassword(String value) {
    String password = value.trim();
    if (password.isEmpty) {
      _password = ValidationItem(
        error: 'Password is required',
        isValidating: _password.isValidating,
      );
    }
    if (password.length < 6) {
      _password = ValidationItem(
        error: 'Password must have at least 6 characters',
        isValidating: _password.isValidating,
      );
    } else {
      RegExp upperCaseRegExp = RegExp(r'^(?=.*[A-Z])');
      RegExp lowerCaseRegExp = RegExp(r'^(?=.*[a-z])');
      RegExp numericOrSpecialCharRegExp =
          RegExp(r'^(?:(.*[0-9])|(.*[!@#$&*^-_=+<>?.%]))');
      if (!upperCaseRegExp.hasMatch(password)) {
        _password = ValidationItem(
          error: 'Password must have at least one uppercase letter',
          isValidating: _password.isValidating,
        );
      } else if (!lowerCaseRegExp.hasMatch(password)) {
        _password = ValidationItem(
          error: 'Password must have at least one lowercase letter',
          isValidating: _password.isValidating,
        );
      } else if (!numericOrSpecialCharRegExp.hasMatch(password)) {
        _password = ValidationItem(
          error: 'Password must have at least one number or one symbol',
          isValidating: _password.isValidating,
        );
      } else {
        _password = ValidationItem(
          value: password,
          isValidating: _password.isValidating,
        );
        if (_confirmPassword.value != null &&
            _confirmPassword.value == _password.value) {
          _confirmPassword = ValidationItem(
            value: _confirmPassword.value,
            isValidating: _confirmPassword.isValidating,
          );
        } else if (_confirmPassword.value != null &&
            _confirmPassword.value != _password.value) {
          _confirmPassword = ValidationItem(
            value: _confirmPassword.value,
            error: 'Password must match',
            isValidating: _confirmPassword.isValidating,
          );
        }
      }
    }
    notifyListeners();
  }

  void changeConfirmPassword(String value) {
    String confirmPassword = value.trim();
    if (confirmPassword.isEmpty) {
      _confirmPassword = ValidationItem(
        error: 'Confirm password is required',
        isValidating: _confirmPassword.isValidating,
      );
    } else {
      if (confirmPassword == _password.value) {
        _confirmPassword = ValidationItem(
          value: confirmPassword,
          isValidating: _confirmPassword.isValidating,
        );
      } else {
        _confirmPassword = ValidationItem(
          error: 'Password must match',
          value: confirmPassword,
          isValidating: _confirmPassword.isValidating,
        );
      }
    }
    notifyListeners();
  }

  void changeVerificationCode(String value) {
    String verificationCode = value.trim();
    final RegExp numberRegExp = RegExp(r'^[0-9]+$');
    if (verificationCode.isEmpty) {
      _verificationCode = ValidationItem(
        error: 'Verification code is required',
        isValidating: _verificationCode.isValidating,
      );
    } else if (!numberRegExp.hasMatch(verificationCode)) {
      _verificationCode = ValidationItem(
        error: 'verification code must be number',
        isValidating: _verificationCode.isValidating,
      );
    } else if (verificationCode.length < 6) {
      _verificationCode = ValidationItem(
        error: 'verification code must be 6 numbers',
        isValidating: _verificationCode.isValidating,
      );
    } else {
      _verificationCode = ValidationItem(
        value: verificationCode,
        isValidating: _verificationCode.isValidating,
      );
    }
    notifyListeners();
  }

  void changeSignInPassword(String value) {
    String password = value.trim();
    if (password.isEmpty) {
      _password = ValidationItem(
        error: 'Password is required',
        isValidating: _password.isValidating,
      );
    } else {
      _password = ValidationItem(
        value: password,
        isValidating: _password.isValidating,
      );
    }
    notifyListeners();
  }

  void resetFields() {
    _firstName = ValidationItem(error: 'First name is required');
    _lastName = ValidationItem(error: 'Last name is required');
    _phoneNumber = ValidationItem(error: 'Phone number is required');
    _secondPhoneNumber =
        ValidationItem(error: 'Another phone number is required');
    _password = ValidationItem(error: 'Password is required');
    _confirmPassword = ValidationItem(error: 'Confirm password is required');
    _verificationCode = ValidationItem(error: 'Verification code is required');
    notifyListeners();
  }

  void changeValidationStatus(String value) {
    switch (value) {
      case 'firstName':
        _firstName = ValidationItem(
          isValidating: true,
          value: _firstName.value,
          error: _firstName.error,
        );
        break;
      case 'lastName':
        _lastName = ValidationItem(
          isValidating: true,
          value: _lastName.value,
          error: _lastName.error,
        );
        break;
      case 'phoneNumber':
        _phoneNumber = ValidationItem(
          isValidating: true,
          value: _phoneNumber.value,
          error: _phoneNumber.error,
        );
        break;
      case 'secondPhoneNumber':
        _secondPhoneNumber = ValidationItem(
          isValidating: true,
          value: _secondPhoneNumber.value,
          error: _secondPhoneNumber.error,
        );
        break;
      case 'password':
        _password = ValidationItem(
          isValidating: true,
          value: _password.value,
          error: _password.error,
        );
        break;
      case 'confirmPassword':
        _confirmPassword = ValidationItem(
          isValidating: true,
          value: _confirmPassword.value,
          error: _confirmPassword.error,
        );
        break;
      case 'verificationCode':
        _verificationCode = ValidationItem(
          isValidating: true,
          value: _verificationCode.value,
          error: _verificationCode.error,
        );
        break;
    }
    notifyListeners();
  }

  bool get arePhoneNumbersValid {
    if (_phoneNumber.value != null && secondPhoneNumber.value != null) {
      return true;
    } else {
      return false;
    }
  }

  bool get isVerificationCodeValid {
    if (verificationCode.value != null) {
      return true;
    } else {
      return false;
    }
  }

  bool get isPasswordValid {
    if (password.value != null &&
        password.value != null &&
        password.error == null) {
      return true;
    } else {
      return false;
    }
  }

  bool get areNamesValid {
    if (firstName.value != null && lastName.value != null) {
      return true;
    } else {
      return false;
    }
  }

  bool get isSignInValid {
    if (_phoneNumber.value != null && _password.value != null) {
      return true;
    } else {
      return false;
    }
  }

  bool get isAuthenticated {
    return _accessToken != null && !JwtDecoder.isExpired(_accessToken!);
  }

  Future<void> signIn() async {
    Map<String, dynamic> responseData =
        await _onboardingService.signIn(_phoneNumber.value!, _password.value!);

    _accessToken = responseData['accessToken'] as String;
    _userId = responseData['userId'] as String;
    _driverId = responseData['driverId'] as String;

    await _onboardingService.saveCredentials(
      _accessToken!,
      _userId!,
      _driverId!,
    );
    Driver driver = await loadProfile(_driverId);
    if (driver.status == DriverStatus.INACTIVE) {
      throw HttpException('Your account is not active');
    }
    notifyListeners();
  }

  Future<void> verifyPhoneNumber() async {
    _verificationId =
        await _onboardingService.verifyPhoneNumber(_phoneNumber.value!);
  }

  Future<void> verifyVerificationCode() async {
    await _onboardingService.verifyVerificationCode(
      _verificationCode.value!,
      _verificationId!,
      _phoneNumber.value!,
    );
  }

  Future<void> signUp() async {
    await _onboardingService.signUp(
      _firstName.value!,
      _lastName.value!,
      _phoneNumber.value!,
      _secondPhoneNumber.value!,
      _password.value!,
      _verificationId!,
      _verificationCode.value!,
    );
  }

  Future<bool> tryToLoginAutomatically() async {
    final Map<String, Object>? credentials =
        await _onboardingService.loadCredentials();
    if (credentials == null) return false;
    _accessToken = credentials['accessToken'] as String;
    _userId = credentials['userId'] as String;
    _driverId = credentials['driverId'] as String;
    notifyListeners();
    return true;
  }

  Future<void> signOut() async {
    try {
      await _onboardingService.signOut(_userId!);
    } catch (e) {
      //SignOut failed
    }
    _accessToken = null;
    _userId = null;
    _driverId = null;
    notifyListeners();
    await _onboardingService.clearCredentials();
  }

  Future<Driver> loadProfile(String? driverId) async {
    Driver driver = await _accountController.getProfile(_driverId!);
    setOneSignalExternalUserId(_driverId);
    return driver;
  }

  void setOneSignalExternalUserId(String? driverId) {
    if (driverId != null) {
      OneSignal.shared.setExternalUserId(driverId).then((result) {
        // Log successfully set externalUserId
      }).catchError((error) {
        print(error);
      });
    }
  }
}

class ValidationItem {
  final String? value;
  final String? error;
  final bool isValidating;

  ValidationItem({this.value, this.error, this.isValidating = false});
}
