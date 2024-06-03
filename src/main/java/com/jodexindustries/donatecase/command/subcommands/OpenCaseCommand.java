package com.jodexindustries.donatecase.command.subcommands;

import com.jodexindustries.donatecase.api.Case;
import com.jodexindustries.donatecase.api.data.CaseData;
import com.jodexindustries.donatecase.api.data.SubCommand;
import com.jodexindustries.donatecase.api.data.SubCommandType;
import com.jodexindustries.donatecase.command.GlobalCommand;
import com.jodexindustries.donatecase.tools.Tools;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static com.jodexindustries.donatecase.DonateCase.casesConfig;
import static com.jodexindustries.donatecase.DonateCase.customConfig;

public class OpenCaseCommand implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            String playerName = sender.getName();
            Player player = (Player) sender;
            if (args.length == 1) {
                String caseName = args[0];
                if (Case.hasCaseByType(caseName)) {
                    int keys = Case.getKeys(caseName, playerName);
                    if (keys >= 1) {
                        Case.removeKeys(caseName, playerName, 1);
                        CaseData data = Case.getCase(caseName);
                        if (data == null) return;
                        CaseData.Item winGroup = Tools.getRandomGroup(data);
                        Case.onCaseOpenFinish(data, player, true, winGroup);
                    } else {
                        Tools.msg(player, customConfig.getLang().getString("NoKey"));
                    }
                } else {
                    Tools.msg(sender, Tools.rt(customConfig.getLang().getString("CaseNotExist"), "%case:" + caseName));
                }
            } else {
                GlobalCommand.sendHelp(sender, "dc");
            }
        }
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>(casesConfig.getCases().keySet());
        if(args.length >= 2) {
            return new ArrayList<>();
        }
        return list;
    }

    @Override
    public SubCommandType getType() {
        return SubCommandType.PLAYER;
    }
}