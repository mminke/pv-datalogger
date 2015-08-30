'use strict';

angular.module("userLogin", [
  'userLogin.controllers',
  'userLogin.services'
]);

angular.module('userLogin.controllers', [])
  .controller('loginCtrl', ['$scope', 'loginInfo', function($scope, loginInfo){
    var loginButton = document.getElementsByClassName('login');
    var loginDialog = document.getElementsByClassName('loginDialog');
    $scope.toggleHide = false;
    var toggle = $scope.toggleHide;
    $scope.hideUnhide = function(){
      $('.login').slideUp(300);
      $('.loginDialog').slideDown(300);
    };
    $scope.users = loginInfo.getUsers();
    var user = $scope.users;
    $scope.currentUser = {};
    $scope.loginFunction = function(inputName, inputPass){
      for(var i = 0; i < user.length; i++){
        if(inputName == user[i].username){
          if(inputPass == user[i].password){
            $('.loginDialog').slideUp(200);
            $('.loggedIn').slideDown(200);
            $scope.currentUser = user[i];
          }else{
            $('.failure').delay(200).fadeIn(300);
          }
        }else{
          $('.failure').delay(200).fadeIn(300);
        }
      }
    };
  }])

angular.module('userLogin.services', [])
  .factory('loginInfo', function(){
    return {
      getUsers : function(){
        return [
          {
            name: 'Fred Jones',
            username: 'fred.jones',
            password: 'jones1',
            age: 85,
            homepage: {
              name: 'Google',
              url:'https://www.google.com/'
            },
            foods: ['ham', 'potatoes', 'cheese', 'butter', 'oranges']
          },
          {
            name: 'Jill Smith',
            username: 'jill.smith',
            password: 'smith1',
            age: 56,
            homepage: {
              name: 'Stack Overflow',
              url: 'http://stackoverflow.com/',
            },
            foods: ['cake', 'muffins', 'cookies', 'spinach', 'carrots']
          },
          {
            name: 'Andy Slow',
            username: 'andy.slow',
            password: 'slow1',
            age: 36,
            homepage: {
              name: 'AngularJS',
              url: 'https://angularjs.org/',
            },
            foods: ['grass', 'almonds', 'fruitcake', 'strawberries', 'mangoes']
          }
        ];
      }
    }
});
