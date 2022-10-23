package ga.elirey.locationexplorer.format;

public enum LocationStatus {

    STATUS_APPROACHING, // vehicle is approaching a client to pick him/her up
    STATUS_BOARDING, // vehicle is waiting for the client to board the vehicle
    STATUS_MOVING, // vehicle is moving toward next destination
    STATUS_ARRIVED, // vehicle reached its destination
    STATUS_UNKNOWN // unknown status
}
