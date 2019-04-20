//服务层
app.service('loginService',function($http){
    //登陆后用于查询用户名：
    this.loginName=function(){
        return $http.get('../login/name.do');
    }
});