app.service('uploadService',function($http){
	
	//上传文件
	this.uploadFile=function(){
		var formdata=new FormData();
		formdata.append('file',file.files[0]);//第二个file 文件上传框的name
		
		return $http({
			url:'../upload.do',		
			method:'post',
			data:formdata,
			//anjularjs 对于 post 和 get 请求默认的 Content-Type header 是 application/json。通过设置
    		//‘Content-Type’: undefined，这样浏览器会帮我们把 Content-Type 设置为 multipart/form-data.
			headers:{ 'Content-Type':undefined },
			//通过设置 transformRequest: angular.identity ，anjularjs transformRequest function 将序列化
           //我们的 formdata object.
			transformRequest: angular.identity			
		});
		
	}
	
	
});