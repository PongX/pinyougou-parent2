//购物车服务层
app.service('cartService',function($http){
//购物车列表
    this.findCartList=function(){
        return $http.get('cart/findCartList.do');
    };

    //添加商品到购物车
    this.addGoodsToCartList=function(itemId,num){
        return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
    }

    //合计购物车商品数量以及总价
    this.sum=function (cartList) {
        //定义合计实体类
        var totalValue={totalNum:0,totalMoney:0.00};
        for(var i=0;i<cartList.length;i++){
            var cart=cartList[i];
            for(var j = 0; j <cart.orderItemList.length ; j++) {
                var orderItem=cart.orderItemList[j];//购物车明细
                totalValue.totalNum+=orderItem.num;//购物车里总的商品数量
                totalValue.totalMoney+=orderItem.totalFee;//购物车总的价格
            }
        }
        return totalValue;
    };

    this.findAddressList=function(){
        return $http.get("address/findListByLoginUser.do");
    };

    //保存订单
    this.submitOrder=function(order){
        return $http.post('order/add.do',order);
    }


});