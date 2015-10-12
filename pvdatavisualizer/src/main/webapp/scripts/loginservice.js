
/**
 * Factory to create the loginService singleton. This service keeps track of the currently logged in user and provides methods to manage the login/logout process. 
 */
app.factory('loginService', ['$rootScope', function($rootScope) {

    return {
    	session : { currentUser : null },
    	
    	login : function () {
    		this.session.currentUser = "Anonymous";
    	},
    	logout : function () {
    		this.session.currentUser = null;
    	}
    };

}]);