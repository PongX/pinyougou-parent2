app.controller("searchController",function($scope,searchService,$location) {

    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sortField':'','sort':''};//搜索对象

    //添加搜索项
    $scope.addSearchItem=function(key,value){
        if(key=='category' || key=='brand' || key=='price'){//如果点击的是分类或者是品牌或者价格
            $scope.searchMap[key]=value;//给对应分类或者是品牌属性赋值
            $scope.searchMap.keywords=value;//给对应关键字赋属性赋值，否则输入三星，获取的分类下的其他品牌无法查询
        }else{
            $scope.searchMap.spec[key]=value;//给对应规格属性赋值
        }
        $scope.search();//执行搜索
    };

    //移除复合搜索条件
    $scope.removeSearchItem=function(key){
        if(key=="category" || key=="brand" || key=='price'){//如果是分类或品牌或者价格
            $scope.searchMap[key]="";
        }else{//否则是规格
            delete $scope.searchMap.spec[key];//移除此属性
        }
        $scope.search();//执行搜索
    };

    //搜索
    $scope.search=function () {
        //在执行查询前，默认当前页都为第一页
        $scope.searchMap.pageNo=1;
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap=response;
                buildPageLabel();//调用分页进行展示
            }
        )
    };

    //构建分页标签(totalPages 为总页数)
    buildPageLabel=function(){
        $scope.pageLabel=[]//新增分页栏属性
        var maxPage=$scope.resultMap.totalPages;//总共页数
        var firstPage=1;
        var lastPage=maxPage;
        $scope.firstDot=true;//前面有点
        $scope.lastDot=true;//后边有点
        if(maxPage>5){
            if($scope.searchMap.pageNo<=3){
                lastPage=5;
                $scope.firstDot=false;//前面无点
            }else if ($scope.searchMap.pageNo>=maxPage-2){
                firstPage=maxPage-4;
                $scope.lastDot=false;//后边无点
            }else {
                firstPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2;
            }
        }else {
            $scope.firstDot=false;//前面无点
            $scope.lastDot=false;//后边无点
        }

        //循环产生页码标签
        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    };

    //根据页码查询
    $scope.queryByPage=function(pageNo){
        if (pageNo<1||pageNo>$scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo=pageNo;
        //在执行查询前，把当前页转换为int类型，否则提交到后端有可能变成字符串
        $scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo) ;
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap=response;
                buildPageLabel();//调用分页进行展示
            }
        )
    };

    //判断当前页为第一页
    $scope.isTopPage=function(){
        if($scope.searchMap.pageNo==1){
            return true;
        }else{
            return false;
        }
    };

    //判断当前页是否未最后一页
    $scope.isEndPage=function(){
        if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else{
            return false;
        }
    };

    $scope.sortSearch=function(sortField,sort){
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        $scope.search();
    };

    //判断关键字是不是品牌
    $scope.keywordsIsBrand=function(){
        for(var i=0;i<$scope.resultMap.brandList.length;i++){
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){//
                如果包含
                return true;
            }
        }
        return false;
    }

    //接受初始页面传来的关键字
    //加载查询字符串
    $scope.loadkeywords=function(){
        $scope.searchMap.keywords= $location.search()['keywords'];
        $scope.search();
    }

    
});