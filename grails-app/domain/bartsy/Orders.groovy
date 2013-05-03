package bartsy

class Orders {

    static constraints = {
    }
    
    static belongsTo = [user:UserProfile,item:VenueInventory,orderMadeBy:UserProfile,receiver:UserProfile]
    Date orderTime
    String orderStatus 
    String totalCost
}
