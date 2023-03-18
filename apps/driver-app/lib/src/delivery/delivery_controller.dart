import 'package:flutter/cupertino.dart';
import 'package:vanoma_driver/src/account/account_controller.dart';
import 'package:vanoma_driver/src/delivery/delivery_service.dart';
import 'package:vanoma_driver/src/delivery/models/assignment.dart';
import 'package:vanoma_driver/src/delivery/models/stop.dart';

class DeliveryController with ChangeNotifier {
  DeliveryController(this._deliveryService, this._accountController);
  final DeliveryService _deliveryService;
  final AccountController _accountController;

  String? get driverId => _accountController.driver?.driverId;

  List<Assignment> _assignments = [];
  List<Stop> _stops = [];

  List<Assignment> get assignments => _assignments;
  List<Stop> get stops => _stops;

  int _currentStopIndex = 0;
  int get currentStopIndex => _currentStopIndex;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  Future<void> updateAddress(String parentAddressId, double latitude,
      double longitude, String? houseNumber) async {
    await _deliveryService.updateAddress(
      parentAddressId,
      latitude,
      longitude,
      houseNumber,
    );
  }

  Future<bool> fetchAssignments() async {
    _isLoading = true;
    notifyListeners();
    _assignments = await _deliveryService.fetchAssignments(driverId!);
    _isLoading = false;
    notifyListeners();
    return _assignments.isNotEmpty;
  }

  Future<void> confirmAssignments() async {
    await _deliveryService.confirmAssignments(driverId!, _assignments);
    _assignments = [];
    notifyListeners();
  }

  Future<void> fetchStops() async {
    _isLoading = true;
    notifyListeners();
    _stops = await _deliveryService.fetchStops(driverId!);
    _isLoading = false;
    if (_stops.isNotEmpty) {
      _accountController.getProfile(driverId!);
      int index = 0;
      for (Stop stop in _stops) {
        if (!stop.completedAllTasks) {
          _currentStopIndex = index;
          break;
        }
        index += 1;
      }
    }
    notifyListeners();
  }

  Future<void> confirmDeparture(String stopId) async {
    await _deliveryService.confirmDeparture(stopId);
    int index = _stops.indexWhere((stop) => stop.stopId == stopId);
    _stops[index].hasDeparted = true;
    notifyListeners();
  }

  Future<void> confirmArrival(String stopId) async {
    await _deliveryService.confirmArrival(stopId);
    int index = _stops.indexWhere((stop) => stop.stopId == stopId);
    _stops[index].hasArrived = true;
    notifyListeners();
  }

  Future<bool> completeTask(String taskId) async {
    await _deliveryService.completeTask(taskId);
    Stop stop = _stops[_currentStopIndex];
    int index = stop.tasks.indexWhere((task) => task.taskId == taskId);
    stop.tasks[index].isCompleted = true;
    stop.completedAllTasks = stop.tasks.isNotEmpty &&
        stop.tasks.indexWhere((t) => !t.isCompleted) == -1;
    if (stop.completedAllTasks) {
      if (_currentStopIndex == _stops.length - 1) {
        _stops = [];
        _currentStopIndex = 0;
        _accountController.getProfile(driverId!);
      } else {
        _currentStopIndex += 1;
      }
    }
    notifyListeners();
    return stop.completedAllTasks;
  }

  void clearAssignments() async {
    _assignments = [];
    notifyListeners();
  }
}
