/*
    Copyright 2016-2017 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.core.lifecycle;

import com.willwinder.ugs.nbp.core.control.JogActionService;
import com.willwinder.ugs.nbp.core.control.MacroService;
import com.willwinder.ugs.nbp.core.control.RunActionService;
import com.willwinder.ugs.nbp.core.services.SettingsChangedNotificationService;
import com.willwinder.ugs.nbp.core.statusline.StatusLineService;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.Version;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.modules.OnStart;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author wwinder
 */
@ServiceProvider(service=OptionProcessor.class)
@OnStart
public class startup extends OptionProcessor implements Runnable {
    @Override
    public void run() {
        System.out.println("Loading LocalizingService...");
        Lookup.getDefault().lookup(LocalizingService.class);
        System.out.println("Loading JogService...");
        Lookup.getDefault().lookup(JogActionService.class);
        System.out.println("Loading ActionService...");
        Lookup.getDefault().lookup(RunActionService.class);
        System.out.println("Loading MacroService...");
        Lookup.getDefault().lookup(MacroService.class);
        System.out.println("Loading StatusLineService...");
        Lookup.getDefault().lookup(StatusLineService.class);
        System.out.println("Loading SettingsChangedNotificationService...");
        Lookup.getDefault().lookup(SettingsChangedNotificationService.class);
        System.out.println("Services loaded!");

        System.out.println("Setting UGP version title.");
        Settings settings = CentralLookup.getDefault().lookup(Settings.class);
        setupVersionInformation(settings);
    }

    private void setupVersionInformation(Settings settings) {
        // Only change the window title when all the UI components are fully loaded.
        WindowManager.getDefault().invokeWhenUIReady(() -> {
            String version = Version.getVersion() + " / " + Version.getTimestamp();
            if (settings.isShowNightlyWarning() && version.contains("nightly")) {
                String message =
                        "This version of Universal Gcode Sender is a nightly build.\n"
                        + "It contains all of the latest features and improvements, \n"
                        + "but may also have bugs that still need to be fixed.\n"
                        + "\n"
                        + "If you encounter any problems, please report them on github.";
                JOptionPane.showMessageDialog(new JFrame(), message,
                        "", JOptionPane.INFORMATION_MESSAGE);
            }
            
            String title = Localization.getString("platform-title")
                    + " (" + Localization.getString("version")
                    + " " + version + ")";
            
            WindowManager.getDefault().getMainWindow().setTitle(title);
        });
    }

    private final Option openOption = Option.additionalArguments('o', "open");

    @Override       
    public Set getOptions() {
        HashSet set = new HashSet();
        set.add(openOption);
        return set;
    }

    @Override
    protected void process(Env env, Map<Option, String[]> maps) throws CommandException {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        String inputFile = null;
        int count = 0;
        for (String[] files : maps.values()) {
            for (String file : files) {
                count++;
                inputFile = file;
            }
        }

        if (count == 0 || count > 1) {
            throw new CommandException(1, "Too many input files provided.");
        }

        System.out.println("File to open: " + inputFile);
        try {
            backend.setGcodeFile(new File(inputFile));
        } catch (Exception e) {
            throw new CommandException(1, "Unable to open input file: " + e.getMessage());
        }
    }
}
