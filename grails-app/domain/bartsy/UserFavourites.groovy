package bartsy

class UserFavourites {

    static constraints = {
    }
    
      static belongsTo = [venue:Venue,venueInventory:VenueInventory,userProfile:UserProfile]
}
