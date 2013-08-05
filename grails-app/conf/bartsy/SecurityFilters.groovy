package bartsy

import grails.converters.JSON

class SecurityFilters {

    def filters = {
        all(controller:'*', action:'*') {
            before = {
				def params = JSON.parse(request)
				/*if((!actionName.equals("saveUserProfile") || !actionName.equals("getServerPublicKey") || !actionName.equals("syncUserDetails") || !actionName.equals("bartsyUserLogin") || !actionName.equals("saveVenueDetails"))  && !controllerName.equals("admin")){
					if(params.reqFrom.toString().equals("bartender")){
						def venueInst = Venue.findByVenueId(params.venueId)
						if(venueInst){
							if(venueInst.getSessionCode()!="" && !params.oauthCode.toString().equals(venueInst.getSessionCode())){
								render(text:([errorCode:'10',errorMessage:'Invalid oAuth Token'])as JSON,contentType:"application/json")
								return false
							  }
						}
					}else{
					   def userProfileInst = UserProfile.findByBartsyId(params.bartsyId.toString())
						if(userProfileInst){
							if(userProfileInst.getSessionCode()!="" && !params.oauthCode.toString().equals(userProfileInst.getSessionCode())){
								render(text:([errorCode:'10',errorMessage:'Invalid oAuth Token'])as JSON,contentType:"application/json")
								return false
							 }
						}
					}
				}*/				
				
				if (!session.user && controllerName.equals("admin") && !actionName.equals('adminLogin') && !actionName.equals('index')) {
					forward(controller:"admin", action:"index")
					return false
				 }
            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
    }
}
