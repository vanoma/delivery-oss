import 'dart:convert';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:http/http.dart' as http;
import 'package:jwt_decoder/jwt_decoder.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:vanoma_driver/src/models/http_exception.dart';

class HttpService {
  static const _host =
      String.fromEnvironment('API_URL', defaultValue: 'staging.api.vanoma.com');
  static const String baseUrl = 'https://$_host';

  Future<Map<String, String>> get _headers async {
    String? authHeader = await _authorizationHeader;
    if (authHeader != null) {
      return {
        'Content-Type': 'application/json',
        'Authorization': authHeader,
      };
    }
    return headersNoAuthHeader;
  }

  Map<String, String> get headersNoAuthHeader {
    return {
      'Content-Type': 'application/json',
    };
  }

  Map<String, String> cookies = {};

  static const int STATUS_200_OK = 200;
  static const int STATUS_201_CREATED = 201;
  static const int STATUS_400_BAD_REQUEST = 400;
  static const int STATUS_401_UNAUTHORIZED = 401;
  static const int STATUS_403_FORBIDDEN = 403;
  static const int STATUS_500_SERVER_ERROR = 500;
  static const String SERVER_ERROR_MESSAGE =
      "There was an issue. Please contact support.";
  static const String AUTHENTICATION_ERROR =
      'Something went wrong. Contact support.';

  Future<http.Response> post(String endpoint, Map<String, dynamic> body,
      {bool withAuthHeader = true}) async {
    Map<String, String> headers =
        withAuthHeader ? await _headers : headersNoAuthHeader;
    String? myCookies = await getCookies();
    if (myCookies != null) {
      headers['cookie'] = myCookies;
    }
    final http.Response response = await http.post(
      Uri.parse('$baseUrl$endpoint'),
      body: json.encode(body),
      headers: headers,
    );
    _updateCookie(response);
    return response;
  }

  Future<http.Response?> get(String endpoint) async {
    Map<String, String> headers = await _headers;
    String? myCookies = await getCookies();
    if (myCookies != null) {
      headers['cookie'] = myCookies;
    }
    Map<String, String> getHeaders = Map.from(headers);
    getHeaders.remove('Content-Type');
    try {
      final http.Response response = await http.get(
        Uri.parse('$baseUrl$endpoint'),
        headers: getHeaders,
      );
      _updateCookie(response);
      return response;
    } catch (e) {}
  }

  Future<http.Response> patch(
      String endpoint, Map<String, dynamic> body) async {
    Map<String, String> headers = await _headers;

    String? myCookies = await getCookies();
    if (myCookies != null) {
      headers['cookie'] = myCookies;
    }
    final http.Response response = await http.patch(
      Uri.parse('$baseUrl$endpoint'),
      body: json.encode(body),
      headers: headers,
    );
    _updateCookie(response);
    return response;
  }

  Future<void> _updateCookie(http.Response response) async {
    String? allSetCookie = response.headers['set-cookie'];
    const storage = FlutterSecureStorage();

    if (allSetCookie != null) {
      var setCookies = allSetCookie.split(',');

      for (var setCookie in setCookies) {
        var cookies = setCookie.split(';');

        for (var cookie in cookies) {
          _setCookie(cookie);
        }
      }
      await storage.write(key: 'myCookies', value: _generateCookieHeader());
    }
  }

  void _setCookie(String rawCookie) {
    if (rawCookie.isNotEmpty) {
      var keyValue = rawCookie.split('=');
      if (keyValue.length == 2) {
        var key = keyValue[0].trim();
        var value = keyValue[1];

        // ignore keys that aren't cookies
        if (key == 'path' || key == 'expires') return;

        cookies[key] = value;
      }
    }
  }

  String _generateCookieHeader() {
    String cookie = "";

    for (var key in cookies.keys) {
      if (cookie.isNotEmpty) cookie += ";";
      cookie += key + "=" + cookies[key]!;
    }

    return cookie;
  }

  static Future<String?> getCookies() async {
    const storage = FlutterSecureStorage();

    return await storage.read(key: 'myCookies');
  }

  static void handleResponseNoReturnValue(http.Response response) {
    try {
      if (response.statusCode >= HttpService.STATUS_400_BAD_REQUEST &&
          response.statusCode < HttpService.STATUS_500_SERVER_ERROR) {
        Map<String, dynamic> responseData = json.decode(response.body);
        throw HttpException(responseData['errorMessage'] as String);
      } else if (response.statusCode >= HttpService.STATUS_500_SERVER_ERROR) {
        throw HttpException(HttpService.SERVER_ERROR_MESSAGE);
      } // TODO If statusCode is 401 we need to take them to Login page. Always.
    } on HttpException catch (error) {
      throw HttpException(error.toString());
    } catch (_) {
      throw HttpException(HttpService.AUTHENTICATION_ERROR);
    }
  }

  static Map<String, dynamic> getResponseDataFromResponse(
      http.Response? response) {
    if (response == null) throw HttpException('Network error');
    try {
      Map<String, dynamic> responseData = json.decode(response.body);
      if (response.statusCode >= HttpService.STATUS_200_OK &&
          response.statusCode < 300) {
        return responseData;
      } else if (response.statusCode >= HttpService.STATUS_400_BAD_REQUEST &&
          response.statusCode < HttpService.STATUS_500_SERVER_ERROR) {
        throw HttpException(responseData['errorMessage'] as String);
      } else if (response.statusCode >= HttpService.STATUS_500_SERVER_ERROR) {
        throw HttpException(HttpService.SERVER_ERROR_MESSAGE);
      } // TODO If statusCode is 401 we need to take them to Login page. Always.
    } on HttpException catch (error) {
      throw HttpException(error.toString());
    }
    throw HttpException(HttpService.AUTHENTICATION_ERROR);
  }

  Future<String?> get accessToken async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    if (!prefs.containsKey('credentials')) {
      return null;
    }
    Map<String, dynamic> credentials =
        json.decode(prefs.getString('credentials')!);
    String? accessToken = credentials['accessToken'] as String;
    String? userId = credentials['userId'] as String;
    if (JwtDecoder.isExpired(accessToken)) {
      accessToken = await refreshToken(userId);
      if (accessToken == null) return null;
      prefs.setString(
          'credentials',
          json.encode({
            'accessToken': accessToken,
            'userId': userId,
            'driverId': credentials['driverId'],
          }));
    }
    return accessToken;
  }

  Future<String?> get _authorizationHeader async {
    String? _accessToken = await accessToken;
    if (_accessToken == null) return _accessToken;
    return "Bearer $_accessToken";
  }

  Future<String?> refreshToken(String userId) async {
    final response = await post('/refresh-token', {}, withAuthHeader: false);
    // If the call fails, it simply logs you out.
    if (response.statusCode >= 200 && response.statusCode < 300) {
      Map<String, dynamic> responseData = json.decode(response.body);
      return responseData['accessToken'] as String;
    }
    return null;
  }
}
