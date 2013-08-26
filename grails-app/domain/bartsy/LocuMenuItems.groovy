package bartsy

class LocuMenuItems {

	static constraints = {
		option_groups(nullable:true)
		options(nullable:true)
		text(nullable:true)
		type(nullable:true)
		price(nullable:true)
		description(nullable:true)
		name(nullable:true)
	}
	static belongsTo = [venue:Venue,locuMenu:LocuMenuName]

	String option_groups
	String options
	String text
	String type
	String price
	String description
	String name
}
