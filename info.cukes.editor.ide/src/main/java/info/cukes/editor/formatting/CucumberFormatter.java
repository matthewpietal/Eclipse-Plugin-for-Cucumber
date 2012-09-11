package info.cukes.editor.formatting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.formatter.IFormattingStrategyExtension;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;

public class CucumberFormatter implements IFormattingStrategy, IFormattingStrategyExtension {
    private static final String SPACE = "    ";

    private IFormattingContext context;
    private String lineDelimiter;
    private String originalString;
    private Set<String> annotations = new HashSet<String>();

    public CucumberFormatter() {
        annotations.add("Given ");
        annotations.add("And ");
        annotations.add("But ");
        annotations.add("When ");
        annotations.add("Then ");
    }

    @Override
    public void formatterStarts(String initialIndentation) {
        // TODO Auto-generated method stub
    }

    @Override
    public String format(String content, boolean isLineStart, String indentation, int[] positions) {
        return null;
    }

    @Override
    public void formatterStops() {
        this.context = null;
    }

    @Override
    public void format() {
        final IDocument document = (IDocument) context.getProperty(FormattingContextProperties.CONTEXT_MEDIUM);
        final TypedPosition partition = (TypedPosition) context
            .getProperty(FormattingContextProperties.CONTEXT_PARTITION);

        try {
            originalString = document.get(partition.getOffset(), partition.getLength());
            lineDelimiter = TextUtilities.getDefaultLineDelimiter(document);

            MultiTextEdit allEdits = new MultiTextEdit();

            if (partition.getType().equals(IDocument.DEFAULT_CONTENT_TYPE)) {
                formatFeature(allEdits);
                formatScenarios(allEdits);
                formatAnnotations(allEdits);
                formatGrids(allEdits);
            } else {
                // allEdits.addChild(new InsertEdit(partition.getOffset() + partition.getLength(), lineDelimiter));
            }

            allEdits.apply(document);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void formatterStarts(IFormattingContext context) {
        this.context = context;
    }

    private void formatFeature(MultiTextEdit allEdits) throws BadLocationException {
        int featureIndex = originalString.indexOf("Feature:");
        int lastValidChar = indexOfPreviousChar(originalString, featureIndex);

        if (featureIndex > lastValidChar && lastValidChar >= 0) {
            allEdits.addChild(new DeleteEdit(lastValidChar, featureIndex - lastValidChar));
        }
    }

    private void formatScenarios(MultiTextEdit allEdits) throws BadLocationException {
        int lastIndex = -1;
        int scenarioIndex = -1;
        while ((scenarioIndex = originalString.indexOf("Scenario:", lastIndex + 1)) > -1) {
            lastIndex = scenarioIndex;
            int lastValidChar = indexOfPreviousChar(originalString, scenarioIndex);

            if (scenarioIndex > lastValidChar && lastValidChar >= 0) {
                allEdits.addChild(new DeleteEdit(lastValidChar, scenarioIndex - lastValidChar));
                allEdits.addChild(new InsertEdit(lastValidChar, SPACE));
            }
        }
    }

    private void formatAnnotations(MultiTextEdit allEdits) throws BadLocationException {
        for (String annotation : annotations) {
            int lastIndex = -1;
            int index = -1;
            while ((index = originalString.indexOf(annotation, lastIndex + 1)) > -1) {
                lastIndex = index;
                int lastValidChar = indexOfPreviousChar(originalString, index);

                if (index > lastValidChar && lastValidChar >= 0) {
                    allEdits.addChild(new DeleteEdit(lastValidChar, index - lastValidChar));
                }
                allEdits.addChild(new InsertEdit(lastValidChar, SPACE + SPACE));
            }
        }
    }

    private void formatGrids(MultiTextEdit allEdits) throws BadLocationException {
        String[] lines = originalString.split(lineDelimiter);

        List<String> foundGridLines = new ArrayList<String>();

        for (String line : lines) {
            for (int i = 0; i < line.length(); ++i) {
                char charAt = line.charAt(i);
                if (!Character.isWhitespace(charAt)) {
                    if (charAt == '|') {
                        foundGridLines.add(line);
                    } else {
                        processSingleGrid(allEdits, foundGridLines);
                    }
                    break;
                }
            }
        }

        processSingleGrid(allEdits, foundGridLines);
    }

    private void processSingleGrid(MultiTextEdit allEdits, List<String> foundGridLines) throws BadLocationException {
        Map<Integer, Integer> columnNumberToMaxWidth = new HashMap<Integer, Integer>();

        if (foundGridLines.isEmpty()) {
            return;
        }

        int gridStartIndex = originalString.indexOf(foundGridLines.get(0));

        // Find max column width, issue delete instruction for entire line
        for (String line : foundGridLines) {
            String[] columns = line.split("\\|");
            for (int i = 0; i < columns.length; ++i) {
                Integer currentMax = columnNumberToMaxWidth.get(i);
                if (currentMax == null) {
                    currentMax = -1;
                }

                int columnSize = columns[i].trim().length();
                if (columnSize > currentMax) {
                    columnNumberToMaxWidth.put(i, columnSize);
                }
            }

            allEdits.addChild(new DeleteEdit(originalString.indexOf(line), line.length()));
        }

        // Rebuild entire line from scratch with proper spacing
        StringBuilder formattedGrid = new StringBuilder();
        for (String line : foundGridLines) {
            String[] columns = line.split("\\|");
            formattedGrid.append(SPACE).append(SPACE).append(SPACE);
            for (int i = 0; i < columns.length; ++i) {
                int maxWidth = columnNumberToMaxWidth.get(i);
                if (maxWidth == 0) {
                    continue;
                }

                formattedGrid.append("| ");

                String text = columns[i].trim();

                boolean isNumber = true;
                try {
                    new BigDecimal(text);
                } catch (Exception e) {
                    isNumber = false;
                }

                while (text.length() < maxWidth) {
                    if (isNumber) {
                        text = " " + text;
                    } else {
                        text = text + " ";
                    }
                }

                formattedGrid.append(text).append(" ");
            }
            formattedGrid.append("|").append(lineDelimiter);
        }

        allEdits.addChild(new InsertEdit(gridStartIndex, formattedGrid.toString()));

        foundGridLines.clear();
    }

    private int indexOfPreviousChar(String docAsString, int currentOffset) {
        int lastValidCharIndex = currentOffset - 1;

        while (lastValidCharIndex >= 0) {
            char charAt = originalString.charAt(lastValidCharIndex);
            if (!Character.isWhitespace(charAt)) {
                break;
            }

            lastValidCharIndex--;
        }
        lastValidCharIndex++;
        if (lastValidCharIndex != 0) {
            lastValidCharIndex += lineDelimiter.length();
        }

        return lastValidCharIndex;
    }
}
