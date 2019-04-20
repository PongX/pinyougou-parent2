//品牌控制层
app.controller('loginController' ,function($scope,loginService){

    //登陆后用于查询用户名：
    $scope.loginName=function(){
       loginService.loginName().success(function (response) {
           $scope.name=response.loginName;
       })
    };
});	