package es.gob.afirma.standalone.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import es.gob.afirma.core.signers.AOSigner;
import es.gob.afirma.signers.pades.AOPDFSigner;
import es.gob.afirma.standalone.LookAndFeelManager;
import es.gob.afirma.standalone.SimpleAfirmaMessages;
import es.gob.afirma.standalone.ui.SignOperationConfig.CryptoOperation;
import es.gob.afirma.standalone.ui.preferences.AgePolicy;
import es.gob.afirma.standalone.ui.preferences.PreferencesDialog;
import es.gob.afirma.standalone.ui.preferences.PreferencesManager;

public class SignatureConfigInfoPanel extends JPanel {

    /** Serial Id. */
	private static final long serialVersionUID = -2242593726948948111L;

	private JCheckBox pdfVisible = null;

    private JCheckBox pdfStamp = null;

	public SignatureConfigInfoPanel(final SignOperationConfig signConfig, final Color bgColor) {
		createUI(signConfig, bgColor);
	}

	private void createUI(final SignOperationConfig signConfig, final Color bgColor) {

		setBackground(bgColor);

		// Formato de firma
		final JLabel signFormatLabel = new JLabel(
				SimpleAfirmaMessages.getString("SignPanel.103", signConfig.getSignatureFormatName())); //$NON-NLS-1$

		// Resumen de atributos
		final JLabel attrLabel = new JLabel(
				SimpleAfirmaMessages.getString("SignPanel.143")); //$NON-NLS-1$
		final JPanel attrPanel = createAttributesPanel(signConfig);

		// Opciones de firma
		final JPanel optionsPanel = createOptionsPanel(signConfig, bgColor);

		// Agregamos los elementos al panel
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		add(Box.createRigidArea(new Dimension(0, 4)));
		add(signFormatLabel);
        if (attrPanel != null) {
        	attrPanel.setBackground(bgColor);
        	add(Box.createRigidArea(new Dimension(0, 4)));
        	add(attrLabel);
        	add(Box.createRigidArea(new Dimension(0, 4)));
        	add(attrPanel);
        }
        if (optionsPanel != null) {
        	optionsPanel.setBackground(bgColor);
        	add(Box.createRigidArea(new Dimension(0, 4)));
        	add(optionsPanel);
        }
	}

	private static JPanel createAttributesPanel(final SignOperationConfig config) {

		String policyId = null;
		String roles = null;
		boolean definedPlace = false;
		if (config.getExtraParams() != null) {
			final Properties exParams = config.getExtraParams();
			policyId = exParams.getProperty("policyIdentifier"); //$NON-NLS-1$
			roles = exParams.getProperty("signerClaimedRoles"); //$NON-NLS-1$
			definedPlace = exParams.containsKey("signatureProductionStreetAddress") || //$NON-NLS-1$
					exParams.containsKey("signatureProductionCity") || //$NON-NLS-1$
					exParams.containsKey("signatureProductionProvince") || //$NON-NLS-1$
					exParams.containsKey("signatureProductionPostalCode") || //$NON-NLS-1$
					exParams.containsKey("signatureProductionCountry"); //$NON-NLS-1$
		}

		// Para mostrar este apartado debe haber declarada una politica
		// de firma o un rol de firmante
		if (policyId == null && roles == null && !definedPlace) {
			return null;
		}

		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// Resumen de politica de firma
		if (policyId != null) {
			String policyDescription = null;
			if (AgePolicy.isAGEPolicy18(policyId)) {
				policyDescription = SimpleAfirmaMessages.getString("PreferencesPanel.25"); //$NON-NLS-1$
			} else if (AgePolicy.isAGEPolicy19(policyId)) {
				policyDescription = SimpleAfirmaMessages.getString("PreferencesPanel.73"); //$NON-NLS-1$
			} else {
				policyDescription = SimpleAfirmaMessages.getString("PreferencesPanel.26"); //$NON-NLS-1$
			}
			panel.add(new JLabel(" - " + policyDescription)); //$NON-NLS-1$
			panel.add(Box.createRigidArea(new Dimension(0, 4)));
		}

		// Resumen del lugar de firma
		if (definedPlace) {
			final String placeDescription = SimpleAfirmaMessages.getString("SignPanel.149"); //$NON-NLS-1$
			panel.add(new JLabel(" - " + placeDescription)); //$NON-NLS-1$
			panel.add(Box.createRigidArea(new Dimension(0, 4)));
		}

		// Resumen de roles
		if (roles != null) {
			panel.add(new JLabel(" - " + SimpleAfirmaMessages.getString("SignPanel.144", roles))); //$NON-NLS-1$ //$NON-NLS-2$
			panel.add(Box.createRigidArea(new Dimension(0, 4)));
		}

		// Enlace para ver todos los atributos
		panel.add(createAttributesHiperlink(SimpleAfirmaMessages.getString("SignPanel.146"), config)); //$NON-NLS-1$

		return panel;
	}

	private static JLabel createAttributesHiperlink(final String text, final SignOperationConfig signConfig) {

		final JLabel hlLabel = new JLabel(" - " + text); //$NON-NLS-1$

		hlLabel.setFocusable(true);
		hlLabel.setForeground(Color.blue);
    	final Font font = hlLabel.getFont();
    	final Map attributes = font.getAttributes();
    	attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
    	hlLabel.setFont(font.deriveFont(attributes));

    	hlLabel.getAccessibleContext().setAccessibleName(SimpleAfirmaMessages.getString("SignDataPanel.46") + " " + text);  //$NON-NLS-1$//$NON-NLS-2$

		final SignatureAttributesListener linkListener = new SignatureAttributesListener(signConfig);
		hlLabel.addMouseListener(linkListener);
		hlLabel.addFocusListener(linkListener);
		hlLabel.addKeyListener(linkListener);

		return hlLabel;
	}

	private JPanel createOptionsPanel(final SignOperationConfig config, final Color bgColor) {

		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		final AOSigner signer = config.getSigner();
		final CryptoOperation cop = config.getCryptoOperation();
		final FileType fileType = config.getFileType();

		// Agrega opciones adicionales de firma PDF
        if (signer instanceof AOPDFSigner) {

        	// Check para la generacion de firma visible PDF
            this.pdfVisible = new JCheckBox(
        		SimpleAfirmaMessages.getString("SignPanel.44"), //$NON-NLS-1$
        		PreferencesManager.getBoolean(PreferencesManager.PREFERENCE_PADES_VISIBLE)
        	);
            this.pdfVisible.setBackground(bgColor);
            this.pdfVisible.setMnemonic('H');
            panel.add(Box.createRigidArea(new Dimension(0, 4)));
            panel.add(this.pdfVisible);

            // Check para agregar una imagen al PDF antes de la firma
            this.pdfStamp = new JCheckBox(
            		SimpleAfirmaMessages.getString("SignPanel.120"), //$NON-NLS-1$
            		PreferencesManager.getBoolean(PreferencesManager.PREFERENCE_PADES_STAMP)
            	);
            this.pdfStamp.setBackground(bgColor);
            this.pdfStamp.setMnemonic('S');
            panel.add(Box.createRigidArea(new Dimension(0, 4)));
            panel.add(this.pdfStamp);

            if (cop == CryptoOperation.COSIGN) {
            	this.pdfStamp.setSelected(false);
            	this.pdfStamp.setEnabled(false);
            	panel.add(Box.createRigidArea(new Dimension(0, 4)));
            	panel.add(new JLabel(SimpleAfirmaMessages.getString("SignPanel.121"))); //$NON-NLS-1$
            }
        }

        // Agrega boton de opciones avanzadas de multifirma CAdES y XAdES
        if (fileType == FileType.SIGN_CADES || fileType == FileType.SIGN_XADES) {
        	final JButton avanzado = new JButton(SimpleAfirmaMessages.getString("SignPanel.119")); //$NON-NLS-1$

        	panel.add(Box.createRigidArea(new Dimension(0, 6)));
        	avanzado.setMnemonic('a');
        	avanzado.addActionListener(
    			ae -> {
    				if(fileType == FileType.SIGN_CADES) {
    					PreferencesDialog.show(null, true, 2);
    				}
    				else {
    					PreferencesDialog.show(null, true, 3);
    				}
    			}
        	);
        	panel.add(avanzado);
        }

        return panel;
	}

	public boolean isPdfVisibleSignatureSelected() {
		return this.pdfVisible != null && this.pdfVisible.isSelected();
	}

	public boolean isPdfStampSignatureSelected() {
		return this.pdfStamp != null && this.pdfStamp.isSelected();
	}

	static class SignatureAttributesListener implements MouseListener, FocusListener, KeyListener {

		private final SignOperationConfig signConfig;

		public SignatureAttributesListener(final SignOperationConfig signConfig) {
			this.signConfig = signConfig;
		}

		@Override
		public void mouseReleased(final MouseEvent e) { /* No hacemos nada */ }

		@Override
		public void mousePressed(final MouseEvent e) { /* No hacemos nada */ }

		@Override
		public void mouseExited(final MouseEvent e) {
			e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		@Override
		public void mouseEntered(final MouseEvent e) {
			e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}

		@Override
		public void mouseClicked(final MouseEvent e) {
			executeAction((Component) e.getSource(), this.signConfig);
		}

		@Override
		public void focusGained(final FocusEvent e) {
			((JComponent) e.getSource()).setBorder(BorderFactory.createDashedBorder(null, 1, 1));
		}

		@Override
		public void focusLost(final FocusEvent e) {
			((JComponent) e.getSource()).setBorder(null);
		}

		private static void executeAction(final Component component, final SignOperationConfig config) {
			final JDialog dialog = SignatureAttributesDialog.newInstance(component, config);
			dialog.setVisible(true);

		}

		@Override
		public void keyTyped(final KeyEvent e) { /* No hacemos nada */ }

		@Override
		public void keyPressed(final KeyEvent e) { /* No hacemos nada */ }

		@Override
		public void keyReleased(final KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
				executeAction((Component) e.getSource(), this.signConfig);
			}
		}
	}
}
