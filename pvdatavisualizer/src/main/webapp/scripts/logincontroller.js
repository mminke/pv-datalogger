app.controller('LoginController', [
			'$scope', '$rootScope', 'loginService',
			function($scope, $rootScope, loginService) {
				
				$scope.session = loginService.session;
				
				$scope.login = function () {
					loginService.login();
				};
				
				$scope.logout = function () {
					loginService.logout();
				}	
			
			} ]);