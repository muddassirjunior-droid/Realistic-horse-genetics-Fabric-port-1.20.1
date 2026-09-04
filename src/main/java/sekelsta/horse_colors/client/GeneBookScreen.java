package sekelsta.horse_colors.client;

import net.minecraft.client.util.math.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.text.OrderedText;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import sekelsta.horse_colors.HorseColors;
import sekelsta.horse_colors.entity.genetics.Genome;

@Environment(EnvType.CLIENT)
public class GeneBookScreen extends Screen {
    private static final int linesPerPage = 14;
    private static final int lineWrapWidth = 124;
    private static final int bookWidth = 360;
    private static final int bookHeight = 192;
    private static final int pageWidth = 130;
    private static final int pageCrease = 10;
    private static final Identifier BACKGROUND_TEXTURE_LOCATION = new Identifier(HorseColors.MOD_ID + ":textures/gui/book.png");
    int currPage = 0;
    private Genome genome;

    private List<List<String>> contents;
    private List<String> pages;
   /** Holds a copy of the page text, split into page width lines */
    private List<OrderedText> cachedPageLinesLeft = Collections.emptyList();
    private List<OrderedText> cachedPageLinesRight = Collections.emptyList();
    private int cachedPage = -1;

    private PageTurnWidget buttonNextPage;
    private PageTurnWidget buttonPreviousPage;
    /** Determines if a sound is played when the page is turned */
    private final boolean pageTurnSounds = true;

    public GeneBookScreen(Genome genomeIn) {
        super(ScreenTexts.EMPTY);
        this.genome = genomeIn;
    }

    @Override
    public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Render the book picture in the back
        this.renderBackground(guiGraphics);


        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE_LOCATION);

        int x = (this.width - bookWidth) / 2;
        int y = 2;
        //x = 0;
        guiGraphics.drawTexture(BACKGROUND_TEXTURE_LOCATION, x, y, 0, 0, bookWidth, bookHeight, 512, 256);

        if (this.cachedPage != this.currPage) {
            this.cachedPageLinesLeft = cachePageLines(this.currPage);
            this.cachedPageLinesRight = cachePageLines(this.currPage + 1);
        }
        this.cachedPage = this.currPage;

        renderPage(guiGraphics, currPage, cachedPageLinesLeft, this.width / 2 - pageWidth - pageCrease);
        if (this.getPageCount() > currPage + 1) {
            renderPage(guiGraphics, currPage + 1, cachedPageLinesRight, this.width / 2 + pageCrease);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private List<OrderedText> cachePageLines(int page) {
        StringVisitable itextproperties;
        if (page < 0 || page >= this.getPageCount()) {
            itextproperties = StringVisitable.EMPTY;
        }
        else {
            String pagetext = this.getPageText(page);
            itextproperties = StringVisitable.plain(pagetext);
        }
        return this.textRenderer.wrapLines(itextproperties, lineWrapWidth);
    }

    private void renderPage(DrawContext guiGraphics, int pagenum, List<OrderedText> cachedPageLines, int x) {
        String pageindicator = I18n.translate("book.pageIndicator", pagenum + 1, this.getPageCount());

        String pagetext = this.getPageText(pagenum);
        int j1 = this.getTextWidth(pageindicator);
        guiGraphics.drawText(this.textRenderer, pageindicator, (int)(x - j1 + pageWidth), 18, 0, false);

        int lines = Math.min(linesPerPage, cachedPageLines.size());
        for(int i = 0; i < lines; ++i) {
            OrderedText text = cachedPageLines.get(i);
            guiGraphics.drawText(this.textRenderer, text, x, 32 + i * 9, 0, false);
        }
    }

    @Override
    protected void init() {
        this.contents = genome.getBookContents();
        this.pages = new ArrayList<String>();
        for (int ch = 0; ch < contents.size(); ++ch) {
            String s = "";
            int lines = 0;
            for (int ln = 0; ln < contents.get(ch).size(); ++ln) {
                String text = contents.get(ch).get(ln);
                StringVisitable itextproperties = StringVisitable.plain(text);
                // Get the number of lines for this block of text
                int wrapped = this.textRenderer.getTextHandler().wrapLines(itextproperties, lineWrapWidth, Style.EMPTY).size();
                if (lines + wrapped > linesPerPage && lines > 0) {
                    pages.add(s);
                    s = "";
                    lines = 0;
                }
                lines += wrapped;
                s += text + "\n";
            }
            pages.add(s);
        }
        this.addDoneButton();
        this.addChangePageButtons();
    }

    protected void addDoneButton() {
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (p_98299_) -> {
            this.client.setScreen((Screen)null);
        }).dimensions(this.width / 2 - 100, 196, 200, 20).build());
    }

    protected void addChangePageButtons() {
        int x1 = (this.width - bookWidth) / 2;
        int x2 = (this.width + bookWidth) / 2;
        int j = 2;
        int y = 159;
        this.buttonNextPage = this.addDrawableChild(new PageTurnWidget(x2 - 43 - 24, y, true, (p_214159_1_) -> {
            this.nextPage();
        }, this.pageTurnSounds));
        this.buttonPreviousPage = this.addDrawableChild(new PageTurnWidget(x1 + 43, y, false, (p_214158_1_) -> {
            this.previousPage();
        }, this.pageTurnSounds));
        this.updateButtons();
    }

    private int getPageCount() {
        return pages.size();
    }
   /**
    * Moves the display back one page
    */
    protected void previousPage() {
        this.currPage = Math.max(this.currPage - 2, 0);
        this.updateButtons();
    }

   /**
    * Moves the display forward one page
    */
   protected void nextPage() {
      if (this.currPage < this.getPageCount() - 2) {
         this.currPage += 2;
      }

      this.updateButtons();
   }

   private void updateButtons() {
      this.buttonNextPage.visible = this.currPage < this.getPageCount() - 2;
      this.buttonPreviousPage.visible = this.currPage > 0;
   }

    private String getPageText(int pagenum) {
        if (pages.size() > 0) {
            return pages.get(pagenum);
        }
        else {
            return "";
        }
    }

    private int getTextWidth(String text) {
        return this.textRenderer.getWidth(this.textRenderer.isRightToLeft() ? this.textRenderer.mirror(text) : text);
    }
}
