import 'dart:convert';
import 'dart:developer';

import 'package:shared_preferences/shared_preferences.dart';
import 'package:vanoma_driver/src/account/models/driver.dart';
import 'package:vanoma_driver/src/services/http_service.dart';

class AccountService {
  final _httpService = HttpService();

  Future<Driver> fetchProfile(String driverId) async {
    final response = await _httpService.get('/drivers2/$driverId');
    Map<String, dynamic> responseData =
        HttpService.getResponseDataFromResponse(response);
    return Driver.fromJson(responseData);
  }

  Future<void> updateDriverStatus(String driverId, bool isAvailable) async {
    final response = await _httpService
        .patch('/drivers2/$driverId', {'isAvailable': isAvailable});
    HttpService.getResponseDataFromResponse(response);
  }

  Future<DriverStatus> fetchDriverStatus(String driverId) async {
    final response = await _httpService.get('/routing/drivers/$driverId');
    Map<String, dynamic> responseData =
        HttpService.getResponseDataFromResponse(response);
    return Driver.toDriverStatus(responseData['status']);
  }

  Future<Driver?> loadProfile() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    if (!prefs.containsKey('profile')) {
      return null;
    }
    Driver profile = Driver.fromJson(json.decode(prefs.getString('profile')!));

    return profile;
  }

  Future<void> saveProfile(Driver driverProfile) async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setString('profile', json.encode(driverProfile.toJson()));
  }
}
