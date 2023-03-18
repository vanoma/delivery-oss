import 'package:vanoma_driver/src/utils/phone_number_utils.dart';

class Contact {
  final String contactId;
  final String? name;
  final String phoneNumberOne;
  final String phoneNumberTwo;
  final String? parentContactId;

  Contact({
    required this.contactId,
    required this.name,
    required this.phoneNumberOne,
    required this.phoneNumberTwo,
    this.parentContactId,
  });

  factory Contact.fromJson(Map<String, dynamic> json) {
    String? secondNumber = json['phoneNumberTwo'] != null
        ? PhoneNumberUtils.getShortForm(json['phoneNumberTwo'])
        : '';
    return Contact(
      contactId: json['contactId'],
      name: json['name'],
      phoneNumberOne: PhoneNumberUtils.getShortForm(json['phoneNumberOne']),
      phoneNumberTwo: secondNumber,
      parentContactId: json['parentContactId'],
    );
  }
}
