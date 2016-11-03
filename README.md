[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/Zeyad-37/GenericUseCase/blob/master/LICENSE)
# GenericUseCase

Is a library that is a generic implementation of the Domain and Data layers in a clean architecture. 

# Motivation

As developers, we always need to deliver high quality software on time,
 which is not an easy task.
In most tasks, we need to make either a IO operation, whether from the server,
 db or file, which is a lot of boiler plate. And getting it write every time
 is a bit challenging due to the many things that you need to take care of. 
 Like separation of concerns, error handling and writing robust code that 
 would not crash on you.
 I have noticed that this code repeats almost with every user story, and 
 i was basically re-writing the same code, but for different models. So i 
 thought what if i could pass the class with the request and not repeat this
 code over and over. Hence, please welcome the GenericUseCase lib.

# Requirements

Generic Use Case Library can be included in any Android application. 

Generic Use Case Library supports Android 2.3 (Gingerbread) and later. 

# Installation

Provide code examples and explanations of how to get the project.

# Code Example

Get Object From Server:
```
mGenericUseCase.getObject(new GetRequest
        .GetRequestBuilder(OrdersRealmModel.class, true) // true to save result to db, false otherwise.
        .presentationClass(OrderViewModel.class)
        .url(FULL_URL)
        .idColumnName(OrderViewModel.ID)
        .id(mItemId)
        .build())
        .subscribeWith(new DisposableObserver<OrderViewModel>() {
            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(OrderViewModel orderViewModel) {
            }
        });
```
Get Object From DB:
```
mGenericUseCase.getObject(new GetRequest
        .GetRequestBuilder(OrdersRealmModel.class, true)
        .presentationClass(OrderViewModel.class)
        .url("") // empty !!
        .idColumnName(OrderViewModel.ID)
        .id(mItemId)
        .build());
```
Get List From Server:
```
mGenericUseCase.getList(new GetRequest
        .GetRequestBuilder(OrdersRealmModel.class, false)
        .presentationClass(OrderViewModel.class)
        .url(FULL_URL)
        .build())
        .subscribeWith(new DisposableObserver<List<OrderViewModel>>() {
            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(List<OrderViewModel> orderViewModel){
            }
        });
```
Get List From DB:
```
mGenericUseCase.getList(new GetRequest
        .GetRequestBuilder(OrdersRealmModel.class, false)
        .presentationClass(OrderViewModel.class)
        .url("")
        .build());
```
Post/Put Object to Server:
```
mGenericUseCase.postObject(new PostRequest // putObject
        .PostRequestBuilder(OrdersRealmModel.class, true)
        .idColumnName(OrderViewModel.ID)
        .presentationClass(OrderViewModel.class)
        .url(FULL_URL)
        .payLoad(OrderViewModel.toJSONObject()) // or HashMap 
        .build());
```
Post/Put Object to DB:
```
mGenericUseCase.postObject(new PostRequest // putObject
        .PostRequestBuilder(OrdersRealmModel.class, true)
        .idColumnName(OrderViewModel.ID)
        .presentationClass(OrderViewModel.class)
        .url("")
        .payLoad(OrderViewModel.toJSONObject()) // or HashMap 
        .build());
```
Post/Put List to Server:
```
mGenericUseCase.postList(new PostRequest // putList
        .PostRequestBuilder(OrdersRealmModel.class, true)
        .presentationClass(OrdersViewModel.class)
        .payLoad(OrdersViewModel.toJSONArray())
        .idColumnName(OrdersRealmModel.ID)
        .url(FULL_URL)
        .build())
```
Post/Put List to DB:
```
mGenericUseCase.postList(new PostRequest // putList
        .PostRequestBuilder(OrdersRealmModel.class, true)
        .presentationClass(OrdersViewModel.class)
        .payLoad(OrdersViewModel.toJSONArray())
        .idColumnName(OrdersRealmModel.ID)
        .url("")
        .build())
```
Delete All from DB:
```
getGenericUseCase().deleteAll(new PostRequest
        .PostRequestBuilder(OrdersRealmModel.class, true)
        .idColumnName(OrdersRealmModel.ID)
        .url("")
        .build())
```
Upload File
```
mGenericUseCase.uploadFile(new FileIORequest
        .FileIORequestBuilder(FULL_URL, new File())
        .onWifi(true)
        .whileCharging(false)
        .dataClass(OrdersRealmModel.class)
        .presentationClass(OrdersViewModel.class)
        .build())
```
Download File
```
mGenericUseCase.downloadFile(new FileIORequest
        .FileIORequestBuilder(FULL_URL, new File())
        .onWifi(true)
        .whileCharging(false)
        .dataClass(OrdersRealmModel.class)
        .presentationClass(OrdersViewModel.class)
        .build())
```
Read from File
```
mGenericUseCase.readFile(String fullFilePath);
```
Write to File
```
mGenericUseCase.writeToFile(String fullFilePath, String data);
mGenericUseCase.writeToFile(String fullFilePath, byte[] data);
```
Delete Collection from Server
```
getGenericUseCase().deleteCollection(new PostRequest // putList
        .PostRequestBuilder(OrdersRealmModel.class, true)
        .presentationClass(OrdersViewModel.class)
        .payLoad(OrdersViewModel.toJSONArrayOfId())
        .url(FULL_URL)
        .build())
```
# Contributors

Just make pull request. You are in!

# License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
