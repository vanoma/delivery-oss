import 'package:flutter/material.dart';
import 'package:vanoma_driver/src/account/account_service.dart';
import 'package:vanoma_driver/src/account/models/driver.dart';

class AccountController with ChangeNotifier {
  AccountController(this._accountService);
  final AccountService _accountService;

  Driver? _driver;

  Driver? get driver => _driver;

  DriverStatusColor get driverStatusColor {
    if (_driver == null || !_driver!.isAvailable) {
      return DriverStatusColor.UNAVAILABLE;
    }
    return DriverStatusColor.AVAILABLE;
  }

  Future<Driver> getProfile(String driverId) async {
    _driver = await _accountService.loadProfile();
    if (_driver != null) {
      notifyListeners();
    }
    _driver = await _accountService.fetchProfile(driverId);
    notifyListeners();
    await _accountService.saveProfile(_driver!);
    return _driver!;
  }

  Future<void> updateDriverStatus(bool isAvailable) async {
    await _accountService.updateDriverStatus(_driver!.driverId, isAvailable);
    _driver!.isAvailable = isAvailable;
    notifyListeners();
  }
}
