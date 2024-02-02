package com.kemarport.kymsmahindra.helper

object Constants {

    val LONGITUDE: String = "longitude"
    val LATITUDE: String = "latitude"
    val ModelCode: String = "modelcode"
    val ColorCode: String = "colorcode"
    val VinNo: String = "vinno"
    const val KEY_USER_ID = "id"
    const val LOGGEDIN = "loggedIn"
    const val IS_ADMIN = "isAdmin"
    const val USERNAME = "username"
    const val TOKEN = "token"
    const val USER_COORDINATES = "coordinates"
    const val NO_INTERNET = "No Internet Connection"
    const val NETWORK_FAILURE = "Network Failure"
    const val CONFIG_ERROR = "Please configure network details"
    const val INCOMPLETE_DETAILS = "Please fill the required details"
    const val EXCEPTION_ERROR = "No Data Found"
    const val HTTP_ERROR_MESSAGE = "message"
    const val HTTP_HEADER_AUTHORIZATION = "Authorization"
    const val SHARED_PREF = "mahindra_yard_shared_pref"
    const val SERVER_IP = "server_ip"
    const val ISFIRSTTIME = "is_first_time"
    const val SESSION_EXPIRE = "Session Expired ! Please relogin"
    const val KEY_USER_NAME = "userName"
    const val KEY_JWT_TOKEN = "jwtToken"
    const val SERVER_IP_SHARED = "192.168.1.105"

    const val GET = 1
    const val POST = 2
    const val HTTP_OK = 200
    const val HTTP_CREATED = 201
    const val HTTP_EXCEPTION = 202
    const val HTTP_UPDATED = 204
    const val HTTP_FOUND = 302
    const val HTTP_NOT_FOUND = 404
    const val HTTP_CONFLICT = 409
    const val HTTP_INTERNAL_SERVER_ERROR = 500
    const val HTTP_ERROR = 400

    const val DASHBOARD_DATA = "VehicleMilestone/GetVehicleMilestoneDashBoardData"
    const val GET_APP_DETAILS = "MobileApp/GetLatestApkVersion"
    const val DASHBOARD_GRAPH_DATA = "VehicleMilestone/GetDriverwiseVehicleParkedCount"
    const val ADD_LOCATIONS = "LocationMapping/AddLocations"
    const val ADD_DEALERLOCATIONS = "Dealer/AddDealer"
    const val GET_DEALERLOCATIONS = "Dealer/GetAllDealers"
    const val GENERATE_TOKEN = "UserManagement/authenticate"
    const val GET_PARENT_LOCATION = "LocationMapping/GetParentLocation"
    const val GET_YARD_LOCATION = "LocationMapping/GetYardLocation"
    const val LOGIN_URL = "UserManagement/authenticate"
    const val ADD_VEHICLE_RFID_MAPPING = "VehicleRFID/AddVehicleRFIDMapping"
    const val GET_VEHICLE_RFID_MAPPING = "VehicleRFID/GetVehicleRFIDMapping"
    const val PARK_IN_OUT_VEHICLE = "VehicleMilestone/AddVehicleMilestone"
    const val SEARCH_VEHICLE_LIST = "VehicleMilestone/GetVehicleMilestoneandVehicleDetails"


    // const val BASE_URL = "http://192.168.1.23:5000/api/"
    //const val BASE_URL = "http://192.168.1.205:8011/service/api/"
    // const val BASE_URL = "http://103.240.90.141:5050/Service/api/"
    const val BASE_URL_LOCAL = "http://103.240.90.141:5050/Service/api/"

    //dashboard new
    const val VEHICLE_MODEL_CODE = "Vehicle/GetVehicleModelCode"
    const val VEHICLE_COLOR = "Vehicle/GetVehicleColor"
    const val VEHICLE_INVENTORY = "VehicleMilestone/GetVehicleInventory"
    const val VEHICLE_DISPATCH_COUNT = "VehicleMilestone/GetVehicleDispatchCount"
    const val VEHICLE_MANUFACTURED_COUNT = "Vehicle/GetVehiclesManufactured"
    const val VEHICLE_CONFIRMATION_COUNT = "DealerVehicle/GetVehicleConfirmationCount"
    const val VEHICLE_WISE_RECORD = "Dashboard/GetVehicleWiseRecord"

    //new apis 08-11-2023
    //parkrepark vehicles
    const val VEHICLE_PARK_REPARK = "MobileService/postVehicleMovement"
    const val GET_VEHICLE_STATUS = "MobileService/getVehicleStatus"
    const val GET_ALL_INTERNAL_LOCATION = "Location/getAllLocations"

    //setGeofence vehicles
    const val GET_ACTIVE_DEALERS = "MobileService/getActiveDealers"
    const val GET_PARENT_LOCATIONS_NEW = "MobileService/getParentLocations"
    const val GET_CHILD_LOCATIONS = "MobileService/getChildLocations"
    const val POST_GEOFENCE_COORDINATES = "MobileService/postGeofenceCoordinates"


    //prd out list
    const val GET_PRD_OUT_LIST = "Dashboard/getProductionOutList"

    //search vehicle
    const val GET_SEARCH_VEHICLES_LIST = "Dashboard/getSearchVehicleList"
    const val GET_MODEL_CODE = "Dashboard/getVehicleModels"
    const val GET_COLOR_DESCRIPTION = "Dashboard/getVehicleColors"


    //my transaction
    const val GET_MY_TRANSACTION_VEHICLE_LIST = "Dashboard/getMyTransactions"

    //change password
    const val  CHANGE_PASSWORD= "UserManagement/change-password"


    const val LOCATION_ID = "locationId"
    //const val BASE_URL = "http://192.168.1.32:5000/api/"
    const val BASE_URL = "http://103.240.90.141:5050/Service/api/"
    //const val BASE_URL = "http://rfid-yard-lb-1652367993.ap-south-1.elb.amazonaws.com:82/api/"

}