/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2006 Jeff Yoshimi <www.jeffyoshimi.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.simbrain.world.visionworld.sensormatrix.editor;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.simbrain.world.visionworld.Filter;
import org.simbrain.world.visionworld.SensorMatrix;
import org.simbrain.world.visionworld.sensormatrix.SparseSensorMatrix;

/**
 * Sparse sensor matrix editor.
 */
public final class SparseSensorMatrixEditor extends JPanel implements
        SensorMatrixEditor {

    /** Rows. */
    private JTextField rows;

    /** Columns. */
    private JTextField columns;

    /** Receptive field height. */
    private JTextField receptiveFieldHeight;

    /** Receptive field width. */
    private JTextField receptiveFieldWidth;

    /** Effective size. */
    private JLabel effectiveSize;

    /** Display name. */
    private static final String DISPLAY_NAME = "Sparse sensor matrix";

    /** Description. */
    private static final String DESCRIPTION = null;

    /** Default rows. */
    private static final int DEFAULT_ROWS = 10;

    /** Default columns. */
    private static final int DEFAULT_COLUMNS = 10;

    /** Default receptive field height. */
    private static final int DEFAULT_RECEPTIVE_FIELD_HEIGHT = 10;

    /** Default receptive field width. */
    private static final int DEFAULT_RECEPTIVE_FIELD_WIDTH = 10;

    /** Empty insets. */
    private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);

    /** Field insets. */
    private static final Insets FIELD_INSETS = new Insets(0, 0, 6, 0);

    /** Label insets. */
    private static final Insets LABEL_INSETS = new Insets(0, 0, 6, 0);

    /**
     * Create a new sparse sensor matrix editor.
     */
    public SparseSensorMatrixEditor() {
        super();

        initComponents();
        layoutComponents();
    }

    /**
     * Initialize components.
     */
    private void initComponents() {
        rows = new JTextField();
        columns = new JTextField();
        receptiveFieldHeight = new JTextField();
        receptiveFieldWidth = new JTextField();
        effectiveSize = new JLabel();
        DocumentListener updateEffectiveSize = new DocumentListener() {
            /** {@inheritDoc} */
            public void changedUpdate(final DocumentEvent event) {
                updateEffectiveSize();
            }

            /** {@inheritDoc} */
            public void insertUpdate(final DocumentEvent event) {
                updateEffectiveSize();
            }

            /** {@inheritDoc} */
            public void removeUpdate(final DocumentEvent event) {
                updateEffectiveSize();
            }
        };

        rows.getDocument().addDocumentListener(updateEffectiveSize);
        columns.getDocument().addDocumentListener(updateEffectiveSize);
        receptiveFieldHeight.getDocument().addDocumentListener(
                updateEffectiveSize);
        receptiveFieldWidth.getDocument().addDocumentListener(
                updateEffectiveSize);
    }

    /**
     * Layout components.
     */
    private void layoutComponents() {
        setLayout(new GridBagLayout());
        setBorder(new CompoundBorder(new TitledBorder(DISPLAY_NAME),
                new EmptyBorder(6, 6, 6, 6)));
        setToolTipText(DESCRIPTION);
        GridBagConstraints c = new GridBagConstraints();

        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridheight = 1;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.insets = LABEL_INSETS;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.33f;
        c.weighty = 0;
        add(new JLabel("Rows"), c);

        c.insets = FIELD_INSETS;
        c.gridx = 1;
        c.weightx = 0.66f;
        add(rows, c);

        c.insets = LABEL_INSETS;
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.33f;
        add(new JLabel("Columns"), c);

        c.insets = FIELD_INSETS;
        c.gridx = 1;
        c.weightx = 0.66f;
        add(columns, c);

        c.insets = LABEL_INSETS;
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.33f;
        add(new JLabel("Receptive field height"), c);

        c.insets = FIELD_INSETS;
        c.gridx = 1;
        c.weightx = 0.66f;
        add(receptiveFieldHeight, c);

        c.insets = LABEL_INSETS;
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.33f;
        add(new JLabel("Receptive field width"), c);

        c.insets = FIELD_INSETS;
        c.gridx = 1;
        c.weightx = 0.66f;
        add(receptiveFieldWidth, c);

        c.insets = LABEL_INSETS;
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.33f;
        add(new JLabel("Effective size"), c);

        c.insets = FIELD_INSETS;
        c.gridx = 1;
        c.weightx = 0.66f;
        add(effectiveSize, c);

        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = EMPTY_INSETS;
        c.gridx = 0;
        c.gridy++;
        c.weightx = 1.0f;
        c.weighty = 1.0f;
        add(Box.createGlue(), c);
    }

    /**
     * Update the effective size label.
     */
    private void updateEffectiveSize() {
        try {
            int r = Integer.valueOf(rows.getText());
            int c = Integer.valueOf(columns.getText());
            int h = Integer.valueOf(receptiveFieldHeight.getText());
            int w = Integer.valueOf(receptiveFieldWidth.getText());
            effectiveSize.setText((c * w) + "x" + (r * h));
        } catch (NumberFormatException e) {
            effectiveSize.setText("...");
        }
    }

    /** {@inheritDoc} */
    public Component getEditorComponent() {
        rows.setText(String.valueOf(DEFAULT_ROWS));
        columns.setText(String.valueOf(DEFAULT_COLUMNS));
        receptiveFieldHeight.setText(String
                .valueOf(DEFAULT_RECEPTIVE_FIELD_HEIGHT));
        receptiveFieldWidth.setText(String
                .valueOf(DEFAULT_RECEPTIVE_FIELD_WIDTH));
        return this;
    }

    /** {@inheritDoc} */
    public SensorMatrix createSensorMatrix(final Filter defaultFilter)
            throws SensorMatrixEditorException {
        try {
            int r = Integer.valueOf(rows.getText());
            int c = Integer.valueOf(columns.getText());
            int h = Integer.valueOf(receptiveFieldHeight.getText());
            int w = Integer.valueOf(receptiveFieldWidth.getText());
            return new SparseSensorMatrix(r, c, w, h, defaultFilter);
        } catch (NumberFormatException e) {
            throw new SensorMatrixEditorException(e);
        } catch (IllegalArgumentException e) {
            throw new SensorMatrixEditorException(e);
        }
    }

    /** {@inheritDoc} */
    public String toString() {
        return DISPLAY_NAME;
    }
}
