/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.client.kroviz.core.model

import org.apache.causeway.client.kroviz.core.aggregator.AggregatorWithLayout
import org.apache.causeway.client.kroviz.core.event.ResourceProxy
import org.apache.causeway.client.kroviz.core.event.ResourceSpecification
import org.apache.causeway.client.kroviz.layout.Layout
import org.apache.causeway.client.kroviz.to.*
import org.apache.causeway.client.kroviz.ui.core.SessionManager

class ObjectDM(override val title: String) : DisplayModelWithLayout() {

    init {
        layout = ObjectLayout()
    }

    val collectionModelList = mutableListOf<CollectionDM>()
    var data: Exposer? = null
    private var dirty: Boolean = false

    fun setDirty(value: Boolean) {
        dirty = value
    }

    fun addCollectionModel(collectionModel: CollectionDM) {
        val id = collectionModel.id
        val foundModel = collectionModelList.firstOrNull() {
            it.id == id
        }
        if (foundModel == null) {
            collectionModelList.add(collectionModel)
        }
    }

    override fun addLayout(lt: Layout) {
        TODO("Not yet implemented")
    }

    fun getCollectionDisplayModelFor(id: String): CollectionDM {
        console.log("[ODM_getCollectionDisplayModelFor]")
        console.log(id)
        console.log(collectionModelList)
        return collectionModelList.find { it.id == id }!!
    }

    override fun readyToRender(): Boolean {
        return when {
            data == null -> false
            isRendered -> false
            layout == null -> false
            collectionModelList.size == 0 -> false
            else -> layout!!.readyToRender() //collectionsReadyToRender()
        }
    }

    override fun addData(obj: TransferObject, aggregator: AggregatorWithLayout?, referrer: String?) {
        (obj as TObject)
        val exo = Exposer(obj)
        data = exo.dynamise() as? Exposer
        obj.getProperties().forEach { m ->
            val p = createPropertyFrom(m)
            val op = ObjectProperty(p)
            layout?.addObjectProperty(op, aggregator!!, referrer!!)
        }
    }

    fun addResult(resultObject: ResultObject) {
        val tObj = createObjectFrom(resultObject)
        this.addData(tObj)
    }

    override fun getObject(): TObject {
        return (data as Exposer).delegate
    }

    fun save() {
        if (dirty) {
            val tObject = data!!.delegate
            val getLink = tObject.links.first()
            val href = getLink.href
            val reSpec = ResourceSpecification(href)
            val es = SessionManager.getEventStore()
            //WATCHOUT this is sequence dependent: GET and PUT share the same URL - if called after PUTting, it may fail
            val getLogEntry = es.findBy(reSpec)!!
            getLogEntry.setReload()

            val putLink = Link(method = Method.PUT.operation, href = href)
            val logEntry = es.findBy(reSpec)
            val aggregator = logEntry?.getAggregator()!!
            // there may be more than one aggt - which may break this code

            ResourceProxy().fetch(putLink, aggregator)
            // now data should be reloaded
            ResourceProxy().fetch(getLink, aggregator)
        }
    }

    fun undo() {
        if (dirty) {
            reset()
        }
    }

    private fun createPropertyFrom(m: Member): Property {
        return Property(
            id = m.id,
            memberType = m.memberType,
            links = m.links,
            optional = m.optional,
            title = m.id,
            value = m.value,
            extensions = m.extensions,
            format = m.format,
            disabledReason = m.disabledReason
        )
    }

    private fun createObjectFrom(resultObject: ResultObject): TObject {
        val r = resultObject.result!!
        return TObject(
            links = r.links,
            extensions = r.extensions!!,
            title = r.title,
            domainType = r.domainType,
            instanceId = r.instanceId.toString(),
            members = r.members
        )
    }

}
