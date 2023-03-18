import 'package:vanoma_driver/src/delivery/models/address.dart';
import 'package:vanoma_driver/src/delivery/models/contact.dart';

class Package {
  final String packageId;
  final Address fromAddress;
  final Contact fromContact;
  final Address toAddress;
  final Contact toContact;
  final String? fromNote;
  final String? toNote;

  Package({
    required this.packageId,
    required this.fromAddress,
    required this.fromContact,
    required this.toAddress,
    required this.toContact,
    required this.fromNote,
    required this.toNote,
  });

  factory Package.fromJson(Map<String, dynamic> json) {
    return Package(
      packageId: json['packageId'],
      fromAddress: Address.fromJson(json['fromAddress']),
      fromContact: Contact.fromJson(json['fromContact']),
      toAddress: Address.fromJson(json['toAddress']),
      toContact: Contact.fromJson(json['toContact']),
      fromNote: json['fromNote'],
      toNote: json['toNote'],
    );
  }
}
