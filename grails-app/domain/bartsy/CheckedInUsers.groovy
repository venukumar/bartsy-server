package bartsy

class CheckedInUsers {

    static constraints = {
    }
    
    static belongsTo = [userProfile : UserProfile,venue : Venue]
	
	//String status
}
