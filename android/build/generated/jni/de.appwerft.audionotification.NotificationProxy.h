/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2011-2018 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

/** This is generated, do not edit by hand. **/

#include <jni.h>

#include "Proxy.h"

namespace de {
namespace appwerft {
namespace audionotification {
	namespace tiaudionotification {

class NotificationProxy : public titanium::Proxy
{
public:
	explicit NotificationProxy();

	static void bindProxy(v8::Local<v8::Object>, v8::Local<v8::Context>);
	static v8::Local<v8::FunctionTemplate> getProxyTemplate(v8::Isolate*);
	static v8::Local<v8::FunctionTemplate> getProxyTemplate(v8::Local<v8::Context>);
	static void dispose(v8::Isolate*);

	static jclass javaClass;

private:
	static v8::Persistent<v8::FunctionTemplate> proxyTemplate;

	// Methods -----------------------------------------------------------
	static void hide(const v8::FunctionCallbackInfo<v8::Value>&);
	static void stop(const v8::FunctionCallbackInfo<v8::Value>&);
	static void release(const v8::FunctionCallbackInfo<v8::Value>&);
	static void start(const v8::FunctionCallbackInfo<v8::Value>&);
	static void update(const v8::FunctionCallbackInfo<v8::Value>&);
	static void setTitle(const v8::FunctionCallbackInfo<v8::Value>&);
	static void setSubtitle(const v8::FunctionCallbackInfo<v8::Value>&);
	static void setLargeIcon(const v8::FunctionCallbackInfo<v8::Value>&);

	// Dynamic property accessors ----------------------------------------

};

	} // namespace tiaudionotification
} // audionotification
} // appwerft
} // de
