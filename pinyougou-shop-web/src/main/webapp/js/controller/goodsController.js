 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
	    //获得通过地址栏传递的参数id, ?前要加#
	    var id = $location.search()["id"];
	    if(id==null){//不为null说明从goods.html里修改过来的
	        return;
        }
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
                //向富文本编辑器添加商品介绍
                editor.html($scope.entity.tbGoodsDesc.introduction);
                //显示图片列表
                $scope.entity.tbGoodsDesc.itemImages= JSON.parse($scope.entity.tbGoodsDesc.itemImages);
                //显示扩展属性，注意他将和$scope.$watch中的$scope.entity.tbGoodsDesc.customAttributeItems冲突
                //所以需要在$scope.$watch里面添加$location.search()["id"]是否为空的条件
                $scope.entity.tbGoodsDesc.customAttributeItems= JSON.parse($scope.entity.tbGoodsDesc.customAttributeItems);
                //显示规格如[{"attributeValue":["16G","64G","128G"],"attributeName":"机身内存"}]
                $scope.entity.tbGoodsDesc.specificationItems=JSON.parse($scope.entity.tbGoodsDesc.specificationItems);
                //SKU 列表规格列转换
                for( var i=0;i<$scope.entity.itemList.length;i++ ){
                    $scope.entity.itemList[i].spec = JSON.parse( $scope.entity.itemList[i].spec);
                }
			}
		);				
	};
    //根据规格名称和选项名称返回是否被勾选
    $scope.checkAttributeValue=function(specName,optionName){
        var items= $scope.entity.tbGoodsDesc.specificationItems;
        var object= $scope.searchObjectByKey(items,'attributeName',specName);
        if(object==null){
            return false;
        }else{
            if(object.attributeValue.indexOf(optionName)>=0){
                return true;
            }else{
                return false;
            }
        }
    }
	
	//保存 
	$scope.save=function(){
        //提取文本编辑器的值
        $scope.entity.tbGoodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.tbGoods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
                    alert('保存成功');
                    location.href="goods.html";//跳转到商品列表页
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};

    //添加商品
    $scope.add=function(){
        $scope.entity.tbGoodsDesc.introduction=editor.html();//提取 kindeditor 编辑器的内容
        goodsService.add($scope.entity).success(
            function(response){
                if(response.success){
                    alert(response.message);
                    $scope.entity={};
                    editor.html('');//清空富文本编辑器
                }else{
                    alert(response.message);
                }
            }
        );
    };

    //上传图片
    $scope.uploadFile=function(){
        uploadService.uploadFile().success(function(response) {
            if(response.success){//如果上传成功，取出 url
                $scope.image_entity.url=response.message;//设置文件地址
            }else{
                alert(response.message);
            }
        }).error(function() {
            alert("上传发生错误");
        });
    };

    $scope.entity={tbGoods:{},tbGoodsDesc:{itemImages:[],specificationItems:[]}};//定义页面实体类结构
    //添加图片列表
    $scope.add_image_entity=function(){
        $scope.entity.tbGoodsDesc.itemImages.push($scope.image_entity);
    }

    //列表中移除图片
    $scope.remove_image_entity=function(index){
        $scope.entity.tbGoodsDesc.itemImages.splice(index,1);
    }

    //读取一级分类
    $scope.selectItemCat1List=function(){
        itemCatService.findByParentId(0).success(
            function(response){
                $scope.itemCat1List=response;
            }
        );
    }

    //读取二级分类,$watch 方法用于监控某个变量的值，当被监控的值发生变化，就自动执行相应的函数newValue为改变后的值, oldValue改变前的值
    $scope.$watch('entity.tbGoods.category1Id', function(newValue, oldValue) {
        //根据选择的值，查询二级分类
        itemCatService.findByParentId(newValue).success(
            function(response){
                $scope.itemCat2List=response;
            }
        );
    });

    //读取三级分类,$watch 方法用于监控某个变量的值，当被监控的值发生变化，就自动执行相应的函数newValue为改变后的值, oldValue改变前的值
    $scope.$watch('entity.tbGoods.category2Id', function(newValue, oldValue) {
        //根据选择的值，查询二级分类
        itemCatService.findByParentId(newValue).success(
            function(response){
                $scope.itemCat3List=response;
            }
        );
    });

    //三级分类选择后 读取模板 ID
    $scope.$watch('entity.tbGoods.category3Id', function(newValue, oldValue) {
        itemCatService.findOne(newValue).success(
            function(response){
                $scope.entity.tbGoods.typeTemplateId=response.typeId; //更新模板 ID
            }
        );
    });

    //模板 ID 选择后 更新品牌列表
    $scope.$watch('entity.tbGoods.typeTemplateId', function(newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
            function(response){
                $scope.typeTemplate=response;//获取类型模板
                $scope.typeTemplate.brandIds= JSON.parse( $scope.typeTemplate.brandIds);//品牌列表
                if($location.search()["id"]==null){
                    $scope.entity.tbGoodsDesc.customAttributeItems=JSON.parse( $scope.typeTemplate.customAttributeItems);//扩展属性列表
                }
            }
        );

        //查询规格列表
        typeTemplateService.findSpecList(newValue).success(
            function(response){
                $scope.specList=response;
            }
        );
    });

    //勾选规格选项和取消规格选项存到：entity.tbGoodsDesc.specificationItems里面
    $scope.updateSpecAttribute=function($event,name,value){
        //调用父方法来从集合中按照 key 查询对象，看是否存在，存在如将{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]}并封装在object里面
        var object= $scope.searchObjectByKey($scope.entity.tbGoodsDesc.specificationItems ,'attributeName', name);
        if(object!=null){
            if($event.target.checked ){
                object.attributeValue.push(value);
            }else{//取消勾选
                object.attributeValue.splice( object.attributeValue.indexOf(value ) ,1);//移除选项
        //如果选项都取消了，将此条记录移除
                if(object.attributeValue.length==0){
                    $scope.entity.tbGoodsDesc.specificationItems.splice($scope.entity.tbGoodsDesc.specificationItems.indexOf(object),1);
                }
            }
        }else{
            $scope.entity.tbGoodsDesc.specificationItems.push(
                {"attributeName":name,"attributeValue":[value]});
        }
    }

    //创建 SKU 列表
    $scope.createItemList=function(){
        $scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0'}];//初始
        //items相当于[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5寸"]}]
        var items= $scope.entity.tbGoodsDesc.specificationItems;
        for(var i=0;i< items.length;i++){
            $scope.entity.itemList =addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue );
        }
    };

    //添加列值
    addColumn=function(list,columnName,conlumnValues){
        var newList=[];//新的集合
        for(var i=0;i<list.length;i++){
            var oldRow= list[i];
            for(var j=0;j<conlumnValues.length;j++){
                var newRow= JSON.parse( JSON.stringify( oldRow ) );//深克隆
                newRow.spec[columnName]=conlumnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    };

    $scope.Status=["未审核","已审核","审核未通过","关闭"]

    $scope.itemCatList=[];

    //加载商品分类列表
    $scope.findItemCatList=function(){
        itemCatService.findAll().success(
            function(response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.itemCatList[response[i].id] = response[i].name;
                }
            })
    }





    
});	
