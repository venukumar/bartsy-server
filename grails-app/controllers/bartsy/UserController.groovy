package bartsy

import grails.converters.JSON

class UserController {

    def index() { }
    
    def saveUserProfile = {
        //def json = JSON.parse(params)
        BartsyUserDetails bartsyUserDetails = new BartsyUserDetails()
        def maxId = BartsyUserDetails.createCriteria().get { projections { max "id" }}
        println maxId 
    }
}
