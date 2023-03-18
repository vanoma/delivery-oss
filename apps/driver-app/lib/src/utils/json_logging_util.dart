import 'dart:convert';

import 'dart:developer';

//This utility is useful when you want to print large dart objects from API
//But in JSON format so that then you can easily format them if you want too
class JsonLoggingUtil {
  static logMapToJson(Map<String, dynamic> map) {
    log(json.encode(map));
  }
}
