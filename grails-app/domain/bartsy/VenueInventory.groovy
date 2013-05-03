package bartsy

class VenueInventory {

    static constraints = {
    }
    
    static belongsTo = [venue:Venue,drinks:Drinks]
    String baseCost
}
