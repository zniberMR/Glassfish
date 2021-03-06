/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 *     05/16/2008-1.0M8 Guy Pelletier 
 *       - 218084: Implement metadata merging functionality between mapping files
 *     12/12/2008-1.1 Guy Pelletier 
 *       - 249860: Implement table per class inheritance support.
 *     02/06/2009-2.0 Guy Pelletier 
 *       - 248293: JPA 2.0 Element Collections (part 2)
 *     06/02/2009-2.0 Guy Pelletier 
 *       - 278768: JPA 2.0 Association Override Join Table
 ******************************************************************************/  
package org.eclipse.persistence.internal.jpa.metadata.accessors.mappings;

import java.util.Vector;

import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.helper.DatabaseTable;
import org.eclipse.persistence.internal.jpa.metadata.MetadataLogger;

import org.eclipse.persistence.internal.jpa.metadata.accessors.classes.ClassAccessor;
import org.eclipse.persistence.internal.jpa.metadata.accessors.objects.MetadataAccessibleObject;
import org.eclipse.persistence.internal.jpa.metadata.accessors.objects.MetadataAnnotation;

import org.eclipse.persistence.mappings.ManyToManyMapping;

/**
 * INTERNAL:
 * A many to many relationship accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class ManyToManyAccessor extends CollectionAccessor {
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public ManyToManyAccessor() {
        super("<many-to-many>");
    }
    
    /**
     * INTERNAL:
     */
    public ManyToManyAccessor(MetadataAnnotation manyToMany, MetadataAccessibleObject accessibleObject, ClassAccessor classAccessor) {
        super(manyToMany, accessibleObject, classAccessor);
        
        setMappedBy((String) manyToMany.getAttribute("mappedBy"));
    }
    
    
    /**
     * INTERNAL:
     * Return the default table to hold the foreign key of a MapKey when
     * and Entity is used as the MapKey
     * @return
     */
    @Override
    protected DatabaseTable getDefaultTableForEntityMapKey(){
        return getJoinTableMetadata().getDatabaseTable();
    }
    
    /**
     * INTERNAL: 
     * Return the logging context for this accessor.
     */
    protected String getLoggingContext() {
        return MetadataLogger.MANY_TO_MANY_MAPPING_REFERENCE_CLASS;
    }
    
    /**
     * INTERNAL:
     */
    @Override
    public boolean isManyToMany() {
        return true;
    }
    
    /**
     * INTERNAL: 
     * A PrivateOwned setting on a ManyToMany is ignored. A log warning is
     * issued.
     */
    @Override
    public boolean isPrivateOwned() {
        if (super.isPrivateOwned()) {
            getLogger().logWarningMessage(MetadataLogger.IGNORE_PRIVATE_OWNED_ANNOTATION, this);
        }
        
        return false;
    }
    
    /**
     * INTERNAL:
     * Process a many to many metadata accessor into a EclipseLink 
     * ManyToManyMapping.
     */
    @Override
    public void process() {
        super.process();
        
        // Create a M-M mapping and process common collection mapping metadata.
        ManyToManyMapping mapping = new ManyToManyMapping();
        process(mapping);

        if (getMappedBy() == null || getMappedBy().equals("")) { 
            // Processing the owning side of a M-M that is process a join table.
            processJoinTable(mapping, getJoinTableMetadata());
        } else {
            // We are processing the a non-owning side of a M-M. Must set the
            // mapping read-only.
            mapping.setIsReadOnly(true);
            
            // Get the owning mapping from the reference descriptor metadata.
            ManyToManyMapping ownerMapping = null;
            if (getOwningMapping(getMappedBy()).isManyToManyMapping()){
                ownerMapping = (ManyToManyMapping)getOwningMapping(getMappedBy());
            } else {
                // If improper mapping encountered, throw an exception.
                throw ValidationException.invalidMapping(getJavaClass(), getReferenceClass());
            }

            // Set the relation table name from the owner.
            mapping.setRelationTable(ownerMapping.getRelationTable());
                 
            // In a table per class inheritance we need to update the target 
            // keys before setting them to mapping's source key fields.
            if (getDescriptor().usesTablePerClassInheritanceStrategy()) {
                // Update the target key fields.
                Vector<DatabaseField> targetKeyFields = new Vector<DatabaseField>();
                for (DatabaseField targetKeyField : ownerMapping.getTargetKeyFields()) {
                    DatabaseField newTargetKeyField = (DatabaseField) targetKeyField.clone();
                    newTargetKeyField.setTable(getDescriptor().getPrimaryTable());
                    targetKeyFields.add(newTargetKeyField);
                }
                
                mapping.setSourceKeyFields(targetKeyFields);
                
                // Update the targetRelationKeyFields.
                Vector<DatabaseField> targetRelationKeyFields = new Vector<DatabaseField>();
                for (DatabaseField targetRelationKeyField : ownerMapping.getTargetRelationKeyFields()) {
                    DatabaseField newTargetRelationKeyField = (DatabaseField) targetRelationKeyField.clone();
                    newTargetRelationKeyField.setTable(getDescriptor().getPrimaryTable());
                    targetRelationKeyFields.add(newTargetRelationKeyField);
                }
                
                mapping.setSourceRelationKeyFields(targetRelationKeyFields);
            } else {
                // Add all the source foreign keys we found on the owner.
                mapping.setSourceKeyFields(ownerMapping.getTargetKeyFields());
                mapping.setSourceRelationKeyFields(ownerMapping.getTargetRelationKeyFields()); 
            }
            
            // Add all the target foreign keys we found on the owner.
            mapping.setTargetKeyFields(ownerMapping.getSourceKeyFields());
            mapping.setTargetRelationKeyFields(ownerMapping.getSourceRelationKeyFields());
        }
    }
}

