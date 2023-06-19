### 概述

> 此项目为街景的后端，主要有街景图片服务、地图瓦片服务、街景图片的组织以及拓扑数据计算入库

* DataController

主要有

1. settlePanoImage负责全景图的组织与元数据入库
2. test（名称忘记改了），负责将表示全景图的位置点，匹配到对应的osm数据上
3. generateCameraTopology，通过特殊设计的地图匹配方法组织其所有全景位置点的拓扑关系，实现前端的点击后全景跳转

* PanoController

/{PanoSet}/getPanoId负责点击获取周边全景影像

/meta/{PanoId}获取该id的全景图元数据，主要负责前端的箭头生成，以及点击后访问哪个全景

/{PanoSet}/{PanoId}根据ID获得具体的全景影像图片

* TileController

获取地图瓦片



注意osm路网数据、以及瓦片数据需要事先准备好!