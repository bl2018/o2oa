var resources = {
        "getEntityManagerContainer": function(){ return {}; },
        "getContext": function(){ return {}; },
        "getApplications": function(){ return {}; },
        "getOrganization": function(){
                return {
                        group: function(){ return {}; },
                        identity: function(){ return {}; },
                        person: function(){ return {}; },
                        personAttribute: function(){ return {}; },
                        role: function(){ return {}; },
                        unit: function(){ return {}; },
                        unitAttribute: function(){ return {}; },
                        unitDuty: function(){ return {}; }
                };
        },
        "getWebservicesClient": function(){ return {}; }
};
var effectivePerson = this.effectivePerson = {};

bind = this;
var library = {
        'version': '4.0',
        "defineProperties": Object.defineProperties || function (obj, properties) {
                return obj;
        },
        'typeOf': function(item){
                if (item == null) return 'null';
                if (item.$family != null) return item.$family();
                if (item.constructor == Array) return 'array';

                if (item.nodeName){
                        if (item.nodeType == 1) return 'element';
                        if (item.nodeType == 3) return (/\S/).test(item.nodeValue) ? 'textnode' : 'whitespace';
                } else if (typeof item.length == 'number'){
                        if (item.callee) return 'arguments';
                        //if ('item' in item) return 'collection';
                }

                return typeof item;
        },

        'JSONDecode': function(string, secure){
                if (!string || library.typeOf(string) != 'string') return null;
                return eval('(' + string + ')');
        },

        'JSONEncode': function(obj){
                if (obj && obj.toJSON) obj = obj.toJSON();
                switch (library.typeOf(obj)){
                        case 'string':
                                return '"' + obj.replace(/[\x00-\x1f\\"]/g, escape) + '"';
                        case 'array':
                                var string = [];
                                for (var i=0; i<obj.length; i++){
                                        var json = library.JSONEncode(obj[i]);
                                        if (json) string.push(json);
                                }
                                return '[' + string + ']';
                        case 'object': case 'hash':
                                var string = [];
                                for (key in obj){
                                        var json = library.JSONEncode(obj[key]);
                                        if (json) string.push(library.JSONEncode(key) + ':' + json);
                                }
                                return '{' + string + '}';
                        case 'number': case 'boolean': return '' + obj;
                        case 'null': return 'null';
                }
                return null;
        }
};
(function(){
        var o={"indexOf": {
                        "value": function(item, from){
                                var length = this.length >>> 0;
                                for (var i = (from < 0) ? Math.max(0, length + from) : from || 0; i < length; i++){
                                        if (this[i] === item) return i;
                                }
                                return -1;
                        }
                }};
        library.defineProperties(Array.prototype, o);
})();

/********************
 this.entityManager; //???????????????
 this.context; //????????????
 this.applications;
 this.org; //????????????
 this.service;//webSerivces?????????

 this.response;
 this.response.seeOther(url); //303??????
 this.response.temporaryRedirect(url); //304??????
 this.response.setBody(body); //????????????

 this.requestText//????????????
 this.request//??????
 this.currentPerson//????????????

 this.Actions;
 this.Actions.load(root);    //??????????????????
 ********************/

bind.entityManager = resources.getEntityManagerContainer();
bind.context = resources.getContext();
bind.applications = resources.getApplications();
bind.organization = resources.getOrganization();
bind.service = resources.getWebservicesClient();

//bind.response = customResponse;
//bind.customResponse = customResponse;
bind.requestText = this.requestText || null;
bind.request = this.request || null;
if (this.effectivePerson) bind.currentPerson = bind.effectivePerson = effectivePerson;

if (this.parameters) bind.parameters = JSON.parse(this.parameters); //JPQL??????????????????
if (this.customResponse){
        var _response = {
                "customResponse": this.customResponse || "",
                seeOther: function(url){
                        customResponse.seeOther(url);
                },
                temporaryRedirect: function(url){
                        customResponse.temporaryRedirect(url);
                },
                setBody: function(o, contentType){
                        var body = o;
                        if (typeOf(o)=="object"){
                                body = JSON.stringify(o);
                        }
                        customResponse.setBody(body, contentType || "");
                }
        };
        bind.response = _response;
}

//????????????
var _define = function(name, fun, overwrite){
        var over = true;
        if (overwrite===false) over = false;
        var o = {};
        o[name] = {"value": fun, "configurable": over};
        library.defineProperties(bind, o);
};

//Action??????
var restfulAcpplication = resources.getApplications();
var _Action = (function(){
        //var actions = [];
        return function(root, json){
                this.actions = json;
                // Object.keys(json).forEach(function(key){
                //     this.actions[key] = json[key];
                // });
                //Object.merge(actions[root], json);
                this.root = root;
                //this.actions = actions[root];

                var invokeFunction = function(service, parameters, key){
                        var _self = this;
                        return function(){
                                var i = parameters.length-1;
                                var n = arguments.length;
                                var functionArguments = arguments;
                                var parameter = {};
                                var success, failure, async, data, file;
                                if (typeOf(functionArguments[0])==="function"){
                                        i=-1;
                                        success = (n>++i) ? functionArguments[i] : null;
                                        failure = (n>++i) ? functionArguments[i] : null;
                                        parameters.each(function(p, x){
                                                parameter[p] = (n>++i) ? functionArguments[i] : null;
                                        });
                                        if (service.method && (service.method.toLowerCase()==="post" || service.method.toLowerCase()==="put")){
                                                data = (n>++i) ? functionArguments[i] : null;
                                        }
                                }else{
                                        parameters.each(function(p, x){
                                                parameter[p] = (n>x) ? functionArguments[x] : null;
                                        });
                                        if (service.method && (service.method.toLowerCase()==="post" || service.method.toLowerCase()==="put")){
                                                data = (n>++i) ? functionArguments[i] : null;
                                        }
                                        success = (n>++i) ? functionArguments[i] : null;
                                        failure = (n>++i) ? functionArguments[i] : null;
                                }
                                return _self.invoke({"name": key, "data": data, "parameter": parameter, "success": success, "failure": failure});
                        };
                };
                var createMethod = function(service, key){
                        var jaxrsUri = service.uri;
                        var re = new RegExp("\{.+?\}", "g");
                        var replaceWords = jaxrsUri.match(re);
                        var parameters = [];
                        if (replaceWords) parameters = replaceWords.map(function(s){
                                return s.substring(1,s.length-1);
                        });

                        this[key] = invokeFunction.call(this, service, parameters, key);
                };
                Object.keys(this.actions).forEach(function(key){
                        var service = this.actions[key];
                        if (service.uri) if (!this[key]) createMethod.call(this, service, key);
                }, this);

                this.invoke = function(option){
                        // {
                        //     "name": "",
                        //     "data": "",
                        //     "parameter": "",,
                        //     "success": function(){}
                        //     "failure": function(){}
                        // }
                        if (this.actions[option.name]){
                                var uri = this.actions[option.name].uri;
                                var method = this.actions[option.name].method || "get";
                                if (option.parameter){
                                        Object.keys(option.parameter).forEach(function(key){
                                                var v = option.parameter[key];
                                                uri = uri.replace("{"+key+"}", v);
                                        });
                                }
                                var res = null;
                                try{
                                        switch (method.toLowerCase()){
                                                case "get":
                                                        res = bind.applications.getQuery(this.root, uri);
                                                        break;
                                                case "post":
                                                        res = bind.applications.postQuery(this.root, uri, JSON.stringify(option.data));
                                                        break;
                                                case "put":
                                                        res = bind.applications.putQuery(this.root, uri, JSON.stringify(option.data));
                                                        break;
                                                case "delete":
                                                        res = bind.applications.deleteQuery(this.root, uri);
                                                        break;
                                                default:
                                                        res = bind.applications.getQuery(this.root, uri);
                                        }
                                        if (res && res.getType().toString()==="success"){
                                                var json = JSON.parse(res.toString());
                                                if (option.success) option.success(json);
                                        }else{
                                                if (option.failure) option.failure(((res) ? JSON.parse(res.toString()) : null));
                                        }
                                        return res;
                                }catch(e){
                                        if (option.failure) option.failure(e);
                                }
                        }
                };
        }
})();
var _Actions = {
        "loadedActions": {},
        "load": function(root){
                if (this.loadedActions[root]) return this.loadedActions[root];
                var jaxrsString = bind.applications.describeApi(root);
                var json = JSON.parse(jaxrsString.toString());
                if (json && json.jaxrs){
                        var actionObj = {};
                        json.jaxrs.each(function(o){
                                if (o.methods && o.methods.length){
                                        var actions = {};
                                        o.methods.each(function(m){

                                                var o = {"uri": "/"+m.uri};
                                                if (m.method) o.method = m.method;
                                                if (m.enctype) o.enctype = m.enctype;
                                                actions[m.name] = o;
                                        }.bind(this));
                                        actionObj[o.name] = new bind.Action(root, actions);
                                }
                        }.bind(this));
                        this.loadedActions[root] = actionObj;
                        return actionObj;
                }
                return null;
        }
};
bind.Actions = _Actions;

//?????????????????????
var _processActions = new _Action("x_processplatform_assemble_surface", {
        "getDictionary": {"uri": "/jaxrs/applicationdict/{applicationDict}/application/{applicationFlag}"},
        "getDictRoot": {"uri": "/jaxrs/applicationdict/{applicationDict}/application/{application}/data"},
        "getDictData": {"uri": "/jaxrs/applicationdict/{applicationDict}/application/{application}/{path}/data"},
        "setDictData": {"uri": "/jaxrs/applicationdict/{applicationDict}/application/{application}/{path}/data", "method": "PUT"},
        "addDictData": {"uri": "/jaxrs/applicationdict/{applicationDict}/application/{application}/{path}/data", "method": "POST"},
        "deleteDictData": {"uri": "/jaxrs/applicationdict/{applicationDict}/application/{application}/{path}/data", "method": "DELETE"},
        "getScript": {"uri": "/jaxrs/script/{flag}/application/{applicationFlag}", "method": "POST"},
});
var _cmsActions = new _Action("x_cms_assemble_control", {
        "getDictionary": {"uri": "/jaxrs/design/appdict/{appDictId}"},
        "getDictRoot": {"uri": "/jaxrs/surface/appdict/{appDictId}/appInfo/{appId}/data"},
        "getDictData": {"uri": "/jaxrs/surface/appdict/{appDictId}/appInfo/{appId}/{path}/data"},
        "setDictData": {"uri": "/jaxrs/surface/appdict/{appDictId}/appInfo/{appId}/{path}/data", "method": "PUT"},
        "addDictData": {"uri": "/jaxrs/surface/appdict/{appDictId}/appInfo/{appId}/{path}/data", "method": "POST"},
        "deleteDictData": {"uri": "/jaxrs/surface/appdict/{appDictId}/appInfo/{appId}/{path}/data", "method": "DELETE"},
        "getDictRootAnonymous" : {"uri": "/jaxrs/anonymous/surface/appdict/{appDictId}/appInfo/{appId}/data"},
        "getDictDataAnonymous" : {"uri": "/jaxrs/anonymous/surface/appdict/{appDictId}/appInfo/{appId}/{path}/data"},
        "getScript": {"uri": "/jaxrs/script/{flag}/appInfo/{appInfoFlag}", "method": "POST"},
});
var _portalActions = new _Action("x_portal_assemble_surface", {
        "getScript":  {"uri": "/jaxrs/script/portal/{portal}/name/{ }","method": "POST"}
});


//include ????????????
//optionsOrName : {
//  type : "", ?????????process, ????????? portal  process  cms
//  application : "", ??????/??????/CMS?????????/??????/id, ?????????????????????
//  name : "" // ????????????/??????/id
//}
//??????name: "" // ????????????/??????/id
var _exec = function(code, _self){
        var returnValue;
        //try{
        if (!_self) _self = this;
        try {
                var f = eval("(function(){return function(){\n"+code+"\n}})();");
                returnValue = f.apply(_self);
        }catch(e){
                console.log("exec", new Error("exec script error"));
                console.log(e);
        }
        return returnValue;
}

var includedScripts = this.includedScripts || {};
this.includedScripts = includedScripts;

var _include = function( optionsOrName , callback ){
        var options = optionsOrName;
        if( typeOf( options ) == "string" ){
                options = { name : options };
        }
        var name = options.name;
        var type = ( options.type && options.application ) ?  options.type : "process";
        var application = options.application

        if (!name || !type || !application){
                console.log("include", new Error("can not find script. missing script name or application"));
                return false;
        }

        if (!includedScripts[application]) includedScripts[application] = [];

        if (includedScripts[application].indexOf( name )> -1){
                if (callback) callback.apply(this);
                return;
        }

        var scriptAction;
        var scriptData;
        switch ( type ){
                case "portal" :
                        _portalActions.getScript( application, name, {"importedList":includedScripts[application]}, function(json){
                                if (json.data){
                                        includedScripts[application] = includedScripts[application].concat(json.data.importedList);
                                        scriptData = json.data;
                                }
                        }.bind(this));
                        break;
                case "process" :
                        _processActions.getScript( name, application, {"importedList":includedScripts[application]}, function(json){
                                if (json.data){
                                        includedScripts[application] = includedScripts[application].concat(json.data.importedList);
                                        scriptData = json.data;
                                }
                        }.bind(this));
                        break;
                case "cms" :
                        _cmsActions.getScript(name, application, {"importedList":includedScripts[application]}, function(json){
                                if (json.data){
                                        includedScripts[application] = includedScripts[application].concat(json.data.importedList);
                                        scriptData = json.data;
                                }
                        }.bind(this));
                        break;
        }
        includedScripts[application].push(name);
        if (scriptData && scriptData.text){
                bind.exec(scriptData.text, this);
                if (callback) callback.apply(this);
        }
};

var _createDict = function(application){
        //optionsOrName : {
        //  type : "", //?????????process, ?????????  process  cms
        //  application : "", //??????/CMS?????????/??????/id, ?????????????????????
        //  name : "", // ??????????????????/??????/id
        //  enableAnonymous : false //????????????????????????????????????CMS???????????????
        //}
        //??????name: "" // ??????????????????/??????/id
        return function(optionsOrName){
                var options = optionsOrName;
                if( typeOf( options ) == "string" ){
                        options = { name : options };
                }
                var name = this.name = options.name;
                var type = ( options.type && options.application ) ?  options.type : "process";
                var applicationId = options.application || application;
                var enableAnonymous = options.enableAnonymous || false;

                //MWF.require("MWF.xScript.Actions.DictActions", null, false);
                if( type == "cms" ){
                        var action = bind.cmsActions;
                }else{
                        var action = bind.processActions;
                }

                var encodePath = function( path ){
                        var arr = path.split(/\./g);
                        var ar = arr.map(function(v){
                                return encodeURIComponent(v);
                        });
                        return ar.join("/");
                };

                this.get = function(path, success, failure){
                        var value = null;
                        if (path){
                                var p = encodePath( path );
                                action[(enableAnonymous && type == "cms") ? "getDictDataAnonymous" : "getDictData"](encodeURIComponent(this.name), applicationId, p, function(json){
                                        value = json.data;
                                        if (success) success(json.data);
                                }, function(xhr, text, error){
                                        if (failure) failure(xhr, text, error);
                                });
                        }else{
                                action[(enableAnonymous && type == "cms") ? "getDictRootAnonymous" : "getDictRoot"](encodeURIComponent(this.name), applicationId, function(json){
                                        value = json.data;
                                        if (success) success(json.data);
                                }, function(xhr, text, error){
                                        if (failure) failure(xhr, text, error);
                                }, false);
                        }

                        return value;
                };

                this.set = function(path, value, success, failure){
                        var p = encodePath( path );
                        //var p = path.replace(/\./g, "/");
                        action.setDictData(encodeURIComponent(this.name), applicationId, p, value, function(json){
                                if (success) success(json.data);
                        }, function(xhr, text, error){
                                if (failure) failure(xhr, text, error);
                        }, false, false);
                };
                this.add = function(path, value, success, failure){
                        var p = encodePath( path );
                        //var p = path.replace(/\./g, "/");
                        action.addDictData(encodeURIComponent(this.name), applicationId, p, value, function(json){
                                if (success) success(json.data);
                        }, function(xhr, text, error){
                                if (failure) failure(xhr, text, error);
                        }, false, false);
                };
                this["delete"] = function(path, success, failure){
                        var p = encodePath( path );
                        //var p = path.replace(/\./g, "/");
                        action.deleteDictData(encodeURIComponent(this.name), applicationId, p, function(json){
                                if (success) success(json.data);
                        }, function(xhr, text, error){
                                if (failure) failure(xhr, text, error);
                        }, false, false);
                };
                this.destory = this["delete"];
        }
};

var getNameFlag = function(name){
        var t = library.typeOf(name);
        if (t==="array"){
                var v = [];
                name.forEach(function(id){
                        v.push((library.typeOf(id)==="object") ? (id.distinguishedName || id.id || id.unique || id.name) : id);
                });
                return v;
        }else{
                return [(t==="object") ? (name.distinguishedName || name.id || name.unique || name.name) : name];
        }
};
var _org = {
        "oGroup": this.organization.group(),
        "oIdentity": this.organization.identity(),
        "oPerson": this.organization.person(),
        "oPersonAttribute": this.organization.personAttribute(),
        "oRole": this.organization.role(),
        "oUnit": this.organization.unit(),
        "oUnitAttribute": this.organization.unitAttribute(),
        "oUnitDuty": this.organization.unitDuty(),

        "group": function() { return this.oGroup},
        "identity": function() { return this.oIdentity},
        "person": function() { return this.oPerson},
        "personAttribute": function() { return this.oPersonAttribute},
        "role": function() { return this.oRole},
        "unit": function() { return this.oUnit},
        "unitAttribute": function() { return this.oUnitAttribute},
        "unitDuty": function() { return this.oUnitDuty},

        "getObject": function(o, v){
                var arr = [];
                if (!v || !v.length){
                        return null;
                }else{
                        for (var i=0; i<v.length; i++){
                                var g = o.getObject(v[i]);
                                if (g) arr.push(JSON.parse(g.toString()));
                        }
                }
                return arr;
        },
        //??????***************
        //????????????--???????????????????????????
        getGroup: function(name){
                var v = this.oGroup.listObject(getNameFlag(name));
                var v_json = (!v || !v.length) ? null: JSON.parse(v.toString());
                return (v_json && v_json.length===1) ? v_json[0] : v_json;
        },

        //??????????????????--???????????????????????????
        //nested  ??????  true???????????????false?????????????????????false???
        listSubGroup: function(name, nested){
                var v = null;
                if (nested){
                        v = this.oGroup.listWithGroupSubNested(getNameFlag(name));
                }else{
                        v = this.oGroup.listWithGroupSubDirect(getNameFlag(name));
                }
                return this.getObject(this.oGroup, v);
        },
        //??????????????????--???????????????????????????
        //nested  ??????  true???????????????false?????????????????????false???
        listSupGroup:function(name, nested){
                var v = null;
                if (nested){
                        v = this.oGroup.listWithGroupSupNested(getNameFlag(name));
                }else{
                        v = this.oGroup.listWithGroupSupDirect(getNameFlag(name));
                }
                return this.getObject(this.oGroup, v);
        },
        //??????????????????????????????--???????????????????????????
        listGroupWithPerson:function(name){
                var v = this.oGroup.listWithPerson(getNameFlag(name));
                return this.getObject(this.oGroup, v);
        },
        //????????????????????????--??????true, false
        groupHasRole: function(name, role){
                nameFlag = (library.typeOf(name)==="object") ? (name.distinguishedName || name.id || name.unique || name.name) : name;
                return this.oGroup.hasRole(nameFlag, getNameFlag(role));
        },

        //??????***************
        //????????????--???????????????????????????
        getRole: function(name){
                var v = this.oRole.listObject(getNameFlag(name));
                var v_json = (!v || !v.length) ? null: JSON.parse(v.toString());
                return (v_json && v_json.length===1) ? v_json[0] : v_json;
        },
        //??????????????????????????????--???????????????????????????
        listRoleWithPerson:function(name){
                var v = this.oRole.listWithPerson(getNameFlag(name));
                return this.getObject(this.oRole, v);
        },

        //??????***************
        //????????????????????????--??????true, false
        personHasRole: function(name, role){
                nameFlag = (library.typeOf(name)==="object") ? (name.distinguishedName || name.id || name.unique || name.name) : name;
                return this.oPerson.hasRole(nameFlag, getNameFlag(role));
        },
        //????????????--???????????????????????????
        getPerson: function(name){
                var v = this.oPerson.listObject(getNameFlag(name));
                var v_json = (!v || !v.length) ? null: JSON.parse(v.toString());
                // if (!v || !v.length) v = null;
                // return (v && v.length===1) ? v[0] : v;
                return (v_json && v_json.length===1) ? v_json[0] : v_json;
        },
        //??????????????????--???????????????????????????
        //nested  ??????  true???????????????false?????????????????????false???
        listSubPerson: function(name, nested){
                var v = null;
                if (nested){
                        v = this.oPerson.listWithPersonSubNested(getNameFlag(name));
                }else{
                        v = this.oPerson.listWithPersonSubDirect(getNameFlag(name));
                }
                return this.getObject(this.oPerson, v);
        },
        //??????????????????--???????????????????????????
        //nested  ??????  true???????????????false?????????????????????false???
        listSupPerson: function(name, nested){
                var v = null;
                if (nested){
                        v = this.oPerson.listWithPersonSupNested(getNameFlag(name));
                }else{
                        v = this.oPerson.listWithPersonSupDirect(getNameFlag(name));
                }
                return this.getObject(this.oPerson, v);
        },
        //???????????????????????????--???????????????????????????
        listPersonWithGroup: function(name){
                var v = this.oPerson.listWithGroup(getNameFlag(name));
                return this.getObject(this.oPerson, v);
                // if (!v || !v.length) v = null;
                // return v;
                // var v_json = (!v || !v.length) ? null: JSON.parse(v.toString());
                // return v_json;
        },
        //???????????????????????????--???????????????????????????
        listPersonWithRole: function(name){
                var v = this.oPerson.listWithRole(getNameFlag(name));
                return this.getObject(this.oPerson, v);
        },
        //???????????????????????????--???????????????????????????
        listPersonWithIdentity: function(name){
                var v = this.oPerson.listWithIdentity(getNameFlag(name));
                return this.getObject(this.oPerson, v);
        },
        //???????????????????????????--???????????????????????????
        getPersonWithIdentity: function(name){
                var v = this.oPerson.listWithIdentity(getNameFlag(name));
                var arr = this.getObject(this.oPerson, v);
                return (arr && arr.length) ? arr[0] : null;
        },
        //???????????????????????????--???????????????????????????
        //nested  ??????  true????????????????????????false?????????????????????false???
        listPersonWithUnit: function(name, nested){
                var v = null;
                if (nested){
                        v = this.oPerson.listWithUnitSubNested(getNameFlag(name));
                }else{
                        v = this.oPerson.listWithUnitSubDirect(getNameFlag(name));
                }
                return this.getObject(this.oPerson, v);
        },

        //????????????************
        //?????????????????????(??????????????????values?????????????????????????????????????????????)
        appendPersonAttribute: function(person, attr, values){
                var personFlag = (library.typeOf(person)==="object") ? (person.distinguishedName || person.id || person.unique || person.name) : person;
                return this.oPersonAttribute.appendWithPersonWithName(personFlag, attr, values);
        },
        //?????????????????????(?????????????????????values??????????????????????????????????????????)
        setPersonAttribute: function(person, attr, values){
                var personFlag = (library.typeOf(person)==="object") ? (person.distinguishedName || person.id || person.unique || person.name) : person;
                return this.oPersonAttribute.setWithPersonWithName(personFlag, attr, values);
        },
        //?????????????????????
        getPersonAttribute: function(person, attr){
                var personFlag = (library.typeOf(person)==="object") ? (person.distinguishedName || person.id || person.unique || person.name) : person;
                var v = this.oPersonAttribute.listAttributeWithPersonWithName(personFlag, attr);
                var v_json = [];
                if (v && v.length){
                        for (var i=0; i<v.length; i++){
                                v_json.push(v[i].toString());
                        }
                }
                return v_json;
        },
        //?????????????????????????????????
        listPersonAttributeName: function(name){
                var p = getNameFlag(name);
                var nameList = [];
                for (var i=0; i<p.length; i++){
                        var v = this.oPersonAttribute.listNameWithPerson(p[i]);
                        if (v && v.length){
                                for (var j=0; j<v.length; j++){
                                        if (nameList.indexOf(v[j])==-1) nameList.push(v[j].toString());
                                }
                        }
                }
                return nameList;
        },
        //???????????????????????????
        //listPersonAllAttribute: function(name){
        // getOrgActions();
        // var data = {"personList":getNameFlag(name)};
        // var v = null;
        // orgActions.listPersonAllAttribute(data, function(json){v = json.data;}, null, false);
        // return v;
        //},

        //??????**********
        //????????????
        getIdentity: function(name){
                var v = this.oIdentity.listObject(getNameFlag(name));
                var v_json = (!v || !v.length) ? null: JSON.parse(v.toString());
                return (v_json && v_json.length===1) ? v_json[0] : v_json;
                // if (!v || !v.length) v = null;
                // return (v && v.length===1) ? v[0] : v;
        },
        //?????????????????????
        listIdentityWithPerson: function(name){
                var v = this.oIdentity.listWithPerson(getNameFlag(name));
                return this.getObject(this.oIdentity, v);
        },
        //????????????????????????--???????????????????????????
        //nested  ??????  true????????????????????????false?????????????????????false???
        listIdentityWithUnit: function(name, nested){
                var v = null;
                if (nested){
                        v = this.oIdentity.listWithUnitSubNested(getNameFlag(name));
                }else{
                        v = this.oIdentity.listWithUnitSubDirect(getNameFlag(name));
                }
                return this.getObject(this.oIdentity, v);
        },

        //??????**********
        //????????????
        getUnit: function(name){
                var v = this.oUnit.listObject(getNameFlag(name));
                var v_json = (!v || !v.length) ? null: JSON.parse(v.toString());
                return (v_json && v_json.length===1) ? v_json[0] : v_json;
                // if (!v || !v.length) v = null;
                // return (v && v.length===1) ? v[0] : v;
        },
        //?????????????????????--???????????????????????????
        //nested  ??????  true???????????????false?????????????????????false???
        listSubUnit: function(name, nested){
                var v = null;
                if (nested){
                        v = this.oUnit.listWithUnitSubNested(getNameFlag(name));
                }else{
                        v = this.oUnit.listWithUnitSubDirect(getNameFlag(name));
                }
                return this.getObject(this.oUnit, v);
        },
        //?????????????????????--???????????????????????????
        //nested  ??????  true???????????????false?????????????????????false???
        listSupUnit: function(name, nested){
                var v = null;
                if (nested){
                        v = this.oUnit.listWithUnitSupNested(getNameFlag(name));
                }else{
                        v = this.oUnit.listWithUnitSupDirect(getNameFlag(name));
                }
                return this.getObject(this.oUnit, v);
        },
        //??????????????????????????????
        //flag ??????    ??????????????????????????????
        //     ?????????  ?????????????????????????????????
        //     ???     ?????????????????????????????????
        getUnitByIdentity: function(name, flag){
                //getOrgActions();
                var getUnitMethod = "current";
                var v;
                if (flag){
                        if (library.typeOf(flag)==="string") getUnitMethod = "type";
                        if (library.typeOf(flag)==="number") getUnitMethod = "level";
                }
                var n = getNameFlag(name)[0];
                switch (getUnitMethod){
                        case "current":
                                v = this.oUnit.getWithIdentity(n);
                                break;
                        case "type":
                                v = this.oUnit.getWithIdentityWithType(n, flag);
                                break;
                        case "level":
                                v = this.oUnit.getWithIdentityWithLevel(n, flag);
                                break;
                }
                var o = this.getObject(this.oUnit, [v]);
                return (o && o.length===1) ? o[0] : o;
        },
        //?????????????????????????????????????????????
        listAllSupUnitWithIdentity: function(name){
                var v = this.oUnit.listWithIdentitySupNested(getNameFlag(name));
                return this.getObject(this.oUnit, v);
        },
        //?????????????????????????????????????????????????????????
        listUnitWithPerson: function(name){
                var v = this.oUnit.listWithPerson(getNameFlag(name));
                return this.getObject(this.oUnit, v);
        },
        //?????????????????????????????????????????????
        listAllSupUnitWithPerson: function(name){
                var v = this.oUnit.listWithPersonSupNested(getNameFlag(name));
                return this.getObject(this.oUnit, v);
        },
        //????????????????????????????????????????????????
        listUnitWithAttribute: function(name, attribute){
                var v = this.oUnit.listWithUnitAttribute(name, attribute);
                return this.getObject(this.oUnit, v);
        },
        //????????????????????????????????????????????????
        listUnitWithDuty: function(name, id){
                var idflag = (library.typeOf(id)==="object") ? (id.distinguishedName || id.id || id.unique || id.name) : id;
                var v = this.oUnit.listWithUnitDuty(name, idflag);
                return this.getObject(this.oUnit, v);
        },

        //????????????***********
        //????????????????????????????????????
        getDuty: function(duty, id){
                var unit = (library.typeOf(id)==="object") ? (id.distinguishedName || id.id || id.unique || id.name) : id;
                var v = this.oUnitDuty.listIdentityWithUnitWithName(unit, duty);
                return this.getObject(this.oIdentity, v);
        },

        //?????????????????????????????????
        listDutyNameWithIdentity: function(name){
                var ids = getNameFlag(name);
                var nameList = [];
                for (var i=0; i<ids.length; i++){
                        var v = this.oUnitDuty.listNameWithIdentity(ids[i]);
                        if (v && v.length){
                                for (var j=0; j<v.length; j++){
                                        if (nameList.indexOf(v[j])==-1) nameList.push(v[j].toString());
                                }
                        }
                }
                return nameList;
        },
        //?????????????????????????????????
        listDutyNameWithUnit: function(name){
                var ids = getNameFlag(name);
                var nameList = [];
                for (var i=0; i<ids.length; i++){
                        var v = this.oUnitDuty.listNameWithUnit(ids[i]);
                        if (v && v.length){
                                for (var j=0; j<v.length; j++){
                                        if (nameList.indexOf(v[j])==-1) nameList.push(v[j].toString());
                                }
                        }
                }
                return nameList;
        },
        //???????????????????????????
        listUnitAllDuty: function(name){
                var u = getNameFlag(name)[0];
                var ds = this.oUnitDuty.listNameWithUnit(u);
                var o = []
                for (var i=0; i<ds.length; i++){
                        v = this.oUnitDuty.listIdentityWithUnitWithName(u, ds[i]);
                        o.push({"name": ds[i], "identityList": this.getObject(this.oIdentity, v)});
                }
                return o;
        },

        //????????????**************
        //?????????????????????(??????????????????values?????????????????????????????????????????????)
        appendUnitAttribute: function(unit, attr, values){
                var unitFlag = (library.typeOf(unit)==="object") ? (unit.distinguishedName || unit.id || unit.unique || unit.name) : unit;
                return this.oUnitAttribute.appendWithUnitWithName(unitFlag, attr, values);
        },
        //?????????????????????(?????????????????????values??????????????????????????????????????????)
        setUnitAttribute: function(unit, attr, values){
                var unitFlag = (library.typeOf(unit)==="object") ? (unit.distinguishedName || unit.id || unit.unique || unit.name) : unit;
                return this.oUnitAttribute.setWithUnitWithName(unitFlag, attr, values);
        },
        //?????????????????????
        getUnitAttribute: function(unit, attr){
                var unitFlag = (library.typeOf(unit)==="object") ? (unit.distinguishedName || unit.id || unit.unique || unit.name) : unit;
                var v = this.oUnitAttribute.listAttributeWithUnitWithName(unitFlag, attr);
                var v_json = [];
                if (v && v.length){
                        for (var i=0; i<v.length; i++){
                                v_json.push(v[i].toString());
                        }
                }
                return v_json;
        },
        //?????????????????????????????????
        listUnitAttributeName: function(name){
                var p = getNameFlag(name);
                var nameList = [];
                for (var i=0; i<p.length; i++){
                        var v = this.oUnitAttribute.listNameWithUnit(p[i]);
                        if (v && v.length){
                                for (var j=0; j<v.length; j++){
                                        if (nameList.indexOf(v[j])==-1) nameList.push(v[j]);
                                }
                        }
                }
                return nameList;
        },
        //???????????????????????????
        listUnitAllAttribute: function(name){
                var u = getNameFlag(name)[0];
                var ds = this.oUnitAttribute.listNameWithUnit(u);
                var o = []
                for (var i=0; i<ds.length; i++){
                        v = this.getUnitAttribute(u, ds[i]);
                        o.push({"name": ds[i], "valueList":v});
                }
                return o;
        }
};

print = function(str, type){}
bind.print = print;

bind.org = _org;
bind.library = library;
bind.define = _define;
bind.Action = _Action;
bind.Actions = _Actions;
bind.processActions = _processActions;
bind.cmsActions = _cmsActions;
bind.portalActions = _portalActions;

bind.exec = _exec;
bind.include = _include;
bind.Dict = _createDict();

