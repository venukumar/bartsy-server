package bartsy

class Messages {

	static mapping = {
		message type:'text'
		status(nullable:true)
	}

	static belongsTo = [sender:UserProfile,receiver:UserProfile,venue:Venue]

	String message
	Date dateCreated
	Date lastUpdated
	int status
}
