package bartsy

class SpecialMenus {

	static constraints = { menuName(nullable:false) }
	static belongsTo =[venue:Venue]
	String menuName
}
