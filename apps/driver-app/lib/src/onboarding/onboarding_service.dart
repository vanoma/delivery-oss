import 'dart:convert';

import 'package:jwt_decoder/jwt_decoder.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:vanoma_driver/src/services/http_service.dart';
import 'package:vanoma_driver/src/utils/json_logging_util.dart';

class OnboardingService {
  final _httpService = HttpService();

  Future<Map<String, dynamic>> signIn(
      String phoneNumber, String password) async {
    final response = await _httpService.post(
        '/sign-in', {'phoneNumber': '25$phoneNumber', 'password': password});
    Map<String, dynamic> responseData =
        HttpService.getResponseDataFromResponse(response);
    return responseData;
  }

  Future<String> verifyPhoneNumber(String phoneNumber) async {
    final response =
        await _httpService.post('/otp', {'phoneNumber': '25$phoneNumber'});
    Map<String, dynamic> responseData =
        HttpService.getResponseDataFromResponse(response);

    JsonLoggingUtil.logMapToJson(responseData);
    return responseData['otpId'] as String;
  }

  Future<void> verifyVerificationCode(
    String verificationCode,
    String verificationId,
    String phoneNumber,
  ) async {
    final response = await _httpService.post(
      '/otp/$verificationId/verification',
      {'otpCode': verificationCode, 'phoneNumber': '25$phoneNumber'},
    );
    HttpService.handleResponseNoReturnValue(response);
  }

  Future<void> signUp(
    String firstName,
    String lastName,
    String phoneNumber,
    String secondPhoneNumber,
    String password,
    String otpId,
    String otpCode,
  ) async {
    final response = await _httpService.post('/drivers2', {
      'firstName': firstName,
      'lastName': lastName,
      'phoneNumber': '25$phoneNumber',
      'secondPhoneNumber': '25$secondPhoneNumber',
      'password': password,
      'otpId': otpId,
      'otpCode': otpCode
    });
    HttpService.getResponseDataFromResponse(response);
  }

  Future<void> signOut(String userId) async {
    final response = await _httpService.post('/sign-out', {});
    HttpService.handleResponseNoReturnValue(response);
  }

  Future<Map<String, String>?> loadCredentials() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    if (!prefs.containsKey('credentials')) {
      return null;
    }
    Map<String, String> credentials =
        Map.from(json.decode(prefs.getString('credentials')!));
    String? accessToken = credentials['accessToken'] as String;
    String? userId = credentials['userId'] as String;
    String? driverId = credentials['driverId'] as String;
    if (JwtDecoder.isExpired(accessToken)) {
      String? newAccessToken = await _httpService.refreshToken(userId);
      if (newAccessToken == null) return null;
      await saveCredentials(newAccessToken, userId, driverId);
      return {
        'accessToken': newAccessToken,
        'userId': userId,
        'driverId': driverId
      };
    }
    return credentials;
  }

  Future<void> saveCredentials(
      String accessToken, String userId, String driverId) async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setString(
        'credentials',
        json.encode(
          {
            'accessToken': accessToken,
            'userId': userId,
            'driverId': driverId,
          },
        ));
  }

  Future<void> clearCredentials() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.remove('credentials');
  }
}
