class Address {
  final String addressId;
  final String? houseNumber;
  final String streetName;
  final String district;
  final double latitude;
  final double longitude;
  final String placeName;
  final String landmark;
  final String parentAddressId;
  final bool isConfirmed;

  Address({
    required this.addressId,
    required this.district,
    required this.latitude,
    required this.longitude,
    required this.houseNumber,
    required this.streetName,
    required this.placeName,
    required this.landmark,
    required this.parentAddressId,
    this.isConfirmed = false,
  });

  factory Address.fromJson(Map<String, dynamic> json) {
    return Address(
      addressId: json['addressId'],
      district: json['district'],
      latitude: json['latitude'],
      longitude: json['longitude'],
      houseNumber: json['houseNumber'] ?? '',
      streetName: json['streetName'] ?? '',
      placeName: json['placeName'] ?? '',
      landmark: json['landmark'] ?? '',
      parentAddressId: json['parentAddressId'] ?? '',
      isConfirmed: json['isConfirmed'],
    );
  }
  String? get addressLine {
    List<String> addressLine = [];
    if (placeName.isNotEmpty) addressLine.add(placeName);
    if (houseNumber != null || houseNumber!.isNotEmpty) {
      addressLine.add(houseNumber!);
    }
    if (streetName.isNotEmpty) addressLine.add(streetName);
    if (addressLine.isNotEmpty) {
      return addressLine.join(", ");
    } else {
      return null;
    }
  }
}
