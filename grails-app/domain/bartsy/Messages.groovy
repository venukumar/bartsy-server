package bartsy

class Messages {

    static constraints = {
		message(size:0..65535)
    }
	
	static belongsTo = [sender:UserProfile,receiver:UserProfile,venue:Venue]
	
	String message
	Date dateCreated
	Date lastUpdated
}
