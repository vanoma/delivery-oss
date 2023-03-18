import 'package:vanoma_driver/src/delivery/models/assignment.dart';
import 'package:vanoma_driver/src/delivery/models/stop.dart';
import 'package:vanoma_driver/src/delivery/models/task.dart';
import 'package:vanoma_driver/src/services/http_service.dart';

class DeliveryService {
  final _httpService = HttpService();

  Future<void> updateAddress(
    String parentAddressId,
    double latitude,
    double longitude,
    String? houseNumber,
  ) async {
    final response = await _httpService.patch(
      '/addresses/$parentAddressId',
      {
        'coordinates': {
          'type': 'Point',
          'coordinates': [latitude, longitude],
        },
        'isConfirmed': true,
        'houseNumber': houseNumber
      },
    );
    Map<String, dynamic> responseData =
        HttpService.getResponseDataFromResponse(response);
  }

  Future<List<Assignment>> fetchAssignments(String driverId) async {
    final response = await _httpService
        .get('/drivers2/$driverId/current-assignments?status=PENDING');
    Map<String, dynamic> responseData =
        HttpService.getResponseDataFromResponse(response);

    List<dynamic> results = responseData['results'];
    List<Assignment> assignments = [];

    for (var a in results) {
      Assignment assignment = Assignment.fromJson(a);
      assignments.add(assignment);
    }
    return assignments;
  }

  Future<void> confirmAssignments(
      String driverId, List<Assignment> assignments) async {
    List<String> assignmentIds = [];
    for (Assignment assignment in assignments) {
      assignmentIds.add(assignment.assignmentId);
    }
    final response = await _httpService.post('/assignment-confirmations',
        {"driverId": driverId, "assignmentIds": assignmentIds});
    HttpService.handleResponseNoReturnValue(response);
  }

  Future<List<Stop>> fetchStops(String driverId) async {
    final response =
        await _httpService.get('/drivers2/$driverId/current-stops');
    Map<String, dynamic> responseData =
        HttpService.getResponseDataFromResponse(response);
    List<dynamic> results = responseData['results'];
    List<Stop> stops = [];
    int index = 0;
    for (var s in results) {
      stops.add(Stop.fromJson(s, index));
      index += 1;
    }
    return stops;
  }

  Future<List<Task>> fetchTasks(String stopId) async {
    final response =
        await _httpService.get('/current-stops/$stopId/current-tasks');
    Map<String, dynamic> responseData =
        HttpService.getResponseDataFromResponse(response);

    List<dynamic> results = responseData['results'];
    List<Task> tasks = [];
    for (var a in results) {
      tasks.add(Task.fromJson(a));
    }
    return tasks;
  }

  Future<void> confirmDeparture(String stopId) async {
    final response =
        await _httpService.post('/current-stops/$stopId/departure', {});
    HttpService.handleResponseNoReturnValue(response);
  }

  Future<void> confirmArrival(String stopId) async {
    final response =
        await _httpService.post('/current-stops/$stopId/arrival', {});
    HttpService.handleResponseNoReturnValue(response);
  }

  Future<void> completeTask(String taskId) async {
    final response =
        await _httpService.post('/current-tasks/$taskId/completion', {});
    HttpService.handleResponseNoReturnValue(response);
  }
}
