app.service('contentService' ,function($http){
	
	//根据广告分类ID查询广告
	this.findByCategoryId=function(categoryId){
		//该路径是指的页面相对于请求的路径：
		return $http.get('content/findByCategoryId.do?categoryId='+categoryId);
	}
	
});