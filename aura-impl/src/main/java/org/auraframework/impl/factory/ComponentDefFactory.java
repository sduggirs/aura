/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.impl.factory;

import java.util.Map;

import javax.xml.stream.XMLStreamReader;

import org.auraframework.adapter.ConfigAdapter;
import org.auraframework.adapter.DefinitionParserAdapter;
import org.auraframework.adapter.ExpressionBuilder;
import org.auraframework.annotations.Annotations.ServiceComponent;
import org.auraframework.def.ComponentDef;
import org.auraframework.def.DefDescriptor;
import org.auraframework.def.Definition;
import org.auraframework.def.StyleDef;
import org.auraframework.impl.root.parser.handler.ComponentDefHandler;
import org.auraframework.impl.root.parser.handler.RootTagHandler;
import org.auraframework.impl.source.AbstractSourceImpl;
import org.auraframework.impl.source.CopiedTextSourceImpl;
import org.auraframework.impl.system.DefDescriptorImpl;
import org.auraframework.service.DefinitionService;
import org.auraframework.system.BundleSource;
import org.auraframework.system.Source;
import org.auraframework.system.TextSource;
import org.auraframework.throwable.quickfix.QuickFixException;

import com.google.common.collect.Maps;

@ServiceComponent
public class ComponentDefFactory extends BaseComponentDefFactory<ComponentDef> {
    @Override
    public Class<ComponentDef> getDefinitionClass() {
        return ComponentDef.class;
    }

    @Override
    public String getMimeType() {
        return "";
    }

    /**
     * @see org.auraframework.impl.factory.BundleBaseFactory#getHandler(org.auraframework.def.DefDescriptor, org.auraframework.system.TextSource, javax.xml.stream.XMLStreamReader, boolean, org.auraframework.service.DefinitionService, org.auraframework.adapter.ConfigAdapter, org.auraframework.adapter.DefinitionParserAdapter, org.auraframework.adapter.ExpressionBuilder)
     */
    @Override
    protected ComponentDefHandler getHandler(DefDescriptor<ComponentDef> descriptor, TextSource<ComponentDef> source,
                                             XMLStreamReader xmlReader, boolean isInInternalNamespace,
                                             DefinitionService definitionService, ConfigAdapter configAdapter,
                                             DefinitionParserAdapter definitionParserAdapter,
                                             ExpressionBuilder expressionBuilder) {
        return new ComponentDefHandler(xmlReader, source, definitionService, isInInternalNamespace,
                configAdapter, definitionParserAdapter, expressionBuilder, descriptor);
    }

    private class TemplateCallbackImpl implements ComponentDefHandler.TemplateCallback {
        BundleSource<ComponentDef> source;
        RootTagHandler<ComponentDef> handler;

        public TemplateCallbackImpl(BundleSource<ComponentDef> source, RootTagHandler<ComponentDef> handler) {
            this.source = source;
            this.handler = handler;
        }

        @Override
        public void callback(boolean isTemplate) throws QuickFixException {
            Map<DefDescriptor<?>, Source<?>> newSourceMap = source.getBundledParts();
            if (isTemplate) {
                DefDescriptor<ComponentDef> descriptor = source.getDescriptor();
                DefDescriptor<StyleDef> oldDesc = new DefDescriptorImpl<>("css", descriptor.getNamespace(),
                            descriptor.getName(), StyleDef.class);
                @SuppressWarnings("unchecked")
                TextSource<StyleDef> cssSource = (TextSource<StyleDef>)newSourceMap.get(
                        new DefDescriptorImpl<>("css", descriptor.getNamespace(),
                            descriptor.getName(), StyleDef.class));
                if (cssSource != null) {
                    DefDescriptor<StyleDef> newDesc = new DefDescriptorImpl<>("templateCss",
                            descriptor.getNamespace(), descriptor.getName(), StyleDef.class);
                    cssSource = new CopiedTextSourceImpl<>(newDesc, cssSource, AbstractSourceImpl.MIME_TEMPLATE_CSS);

                    newSourceMap = Maps.newHashMap(newSourceMap);
                    newSourceMap.remove(oldDesc);
                    newSourceMap.put(newDesc, cssSource);
                }
            }
            Map<DefDescriptor<?>, Definition> defMap = buildDefinitionMap(source.getDescriptor(), newSourceMap);
            handler.setBundledDefs(defMap);
        }
    };

    @Override
    public RootTagHandler<ComponentDef> getDefinitionBuilder(DefDescriptor<ComponentDef> descriptor,
            BundleSource<ComponentDef> source) throws QuickFixException {
        Map<DefDescriptor<?>, Source<?>> sourceMap = source.getBundledParts();
        @SuppressWarnings("unchecked")
        TextSource<ComponentDef> bundleDefSource = (TextSource<ComponentDef>)sourceMap.get(descriptor);
        if (bundleDefSource == null) {
            return null;
        }
        RootTagHandler<ComponentDef> handler = makeHandler(descriptor, bundleDefSource);
        ((ComponentDefHandler)handler).setTemplateCallback(new TemplateCallbackImpl(source, handler));
        return getDefinitionBuilder(descriptor, bundleDefSource, handler);
    }
}
